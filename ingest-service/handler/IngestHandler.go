package handler

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"regexp"
	"strings"
	"time"

	"github.com/saiprasadkrishnamurthy/ingest-service/entity"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/spf13/viper"
	"github.com/twinj/uuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func (serviceFactory *ServiceFactory) HandlePartition(request *model.PartitionFileRequest) model.PartitionFileResponse {
	serviceFactory.Log.Info.Println(" Handle Partition ")
	baseDir := uuid.NewV4().String()
	chunkDir := uuid.NewV4().String()
	defer os.RemoveAll(baseDir)
	defer os.Remove(baseDir)
	defer os.RemoveAll(chunkDir)
	defer os.Remove(chunkDir)

	if err := os.Mkdir(baseDir, os.ModePerm); err != nil {
		log.Fatal(err)
	}
	if err := os.Mkdir(chunkDir, os.ModePerm); err != nil {
		log.Fatal(err)
	}

	chunks := serviceFactory.IngestService.PartitionFiles(request.Name, request.ZipFilePath, baseDir, chunkDir)

	job := entity.IngestJob{
		JobId:             strings.Split(chunks[0], "/")[0],
		Started:           primitive.DateTime(time.Now().UnixMilli()),
		ExpectedChunkSize: len(chunks),
		QueuedChunkSize:   0,
		IngestedChunkSize: 0,
		ReceivedChunkSize: 0,
		Error:             "",
	}
	ingestJobColl := serviceFactory.MongoClient.Database(request.Tenant).Collection("ingestJob")

	result, error := ingestJobColl.InsertOne(context.TODO(), job)
	if error != nil {
		serviceFactory.Log.Error.Printf("Error during insert ingestjob: %s", error)
		return model.PartitionFileResponse{Errors: []model.ApiError{{Code: "ING0002", Message: "Error while creating Ingest Job"}}}
	} else {
		serviceFactory.Log.Info.Printf("Inserted ingestjob: %s", result.InsertedID)
		dbErr := setupDatabase(serviceFactory, request)
		if dbErr == nil {
			for _, chunkFile := range chunks {
				serviceFactory.Log.Info.Println(" About to upload to S3", chunkFile)
				id := strings.Split(chunkFile, "/")[0]
				fileName := strings.Split(chunkFile, "/")[1]
				s3Key := request.Name + "/" + id + "/" + fileName
				serviceFactory.S3Manager.UploadToS3(request.ObjectStorageId, s3Key, chunkFile)
				serviceFactory.Log.Info.Printf(" Uploaded %s to S3 to %s", chunkFile, s3Key)

				json, err := json.Marshal(&model.ChunkCreatedEvent{
					JobId:           id,
					Tenant:          request.Tenant,
					ObjectStorageId: request.ObjectStorageId,
					FileName:        s3Key,
					Name:            request.Name,
					Tags:            request.Tags,
					DataDefinitions: request.DataDefinitions,
				})

				if err == nil {
					publishErr := serviceFactory.Nats.Publish(viper.GetString("chunk_created_event_subject"), []byte(string(json)))
					if publishErr == nil {
						ingestJobColl.UpdateOne(context.TODO(), bson.M{
							"jobid": id,
						}, bson.D{
							{Key: "$inc", Value: bson.D{{Key: "queuedchunksize", Value: 1}}},
						}, options.Update().SetUpsert(false))
						serviceFactory.Log.Info.Printf("Queued Chunk JobId: %s", id)
					} else {
						serviceFactory.Log.Error.Printf("Error while Queueing: %s", publishErr)
					}
				} else {
					serviceFactory.Log.Error.Printf("Error while marshalling %s", err)
				}
			}
		} else {
			return model.PartitionFileResponse{Errors: []model.ApiError{{Code: "ING0003", Message: "Error while setting up ingest database"}}}
		}
	}

	return model.PartitionFileResponse{
		Name:   request.Name,
		Tenant: request.Tenant,
		Chunks: chunks,
	}

}

func setupDatabase(serviceFactory *ServiceFactory, rq *model.PartitionFileRequest) error {
	var colDefinitions string
	for key, element := range rq.DataDefinitions.Definitions {
		col := normaliseString(key)
		if element == "string" {
			colDefinitions += col + " varchar(300)"
		} else if element == "number" {
			colDefinitions += col + " decimal"
		} else if element == "date" {
			colDefinitions += col + " date"
		} else if element == "boolean" {
			colDefinitions += col + " boolean"
		}
		if key == rq.DataDefinitions.IdField {
			colDefinitions += " primary key"
		}
		colDefinitions += ","
	}
	colDefs := strings.TrimSuffix(colDefinitions, ",")

	tableDdl := "create table if not exists " + rq.Tenant + "." + rq.Name + " (" + colDefs + ")"

	fmt.Println(tableDdl)
	fmt.Println()
	for _, key := range rq.DataDefinitions.KeyFields {
		idx := "create index if not exists \"" + rq.Name + "_" + normaliseString(key) + "\" on " + rq.Tenant + "." + rq.Name + " using btree (" + normaliseString(key) + ")"
		serviceFactory.DB.Exec(idx)
	}
	_, error := serviceFactory.DB.Exec(tableDdl)
	return error
}

var nonAlphanumericRegex = regexp.MustCompile(`[^a-zA-Z0-9]+`)

func normaliseString(str string) string {
	return strings.ToLower(nonAlphanumericRegex.ReplaceAllString(str, ""))
}
