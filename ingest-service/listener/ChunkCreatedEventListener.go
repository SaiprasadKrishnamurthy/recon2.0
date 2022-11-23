package listener

import (
	"bufio"
	"context"
	"encoding/json"
	"io/ioutil"
	"os"
	"regexp"
	"strings"

	"github.com/nats-io/nats.go"
	"github.com/saiprasadkrishnamurthy/ingest-service/entity"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/twinj/uuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type ChunkCreatedEventListener struct {
	*BaseListener
	Log *model.Log
}

func (l *ChunkCreatedEventListener) Handle(msg *nats.Msg) {
	var event model.ChunkCreatedEvent
	err := json.Unmarshal(msg.Data, &event)

	if err == nil {
		updateMongo(l.MongoClient, event, "receivedchunksize")
		processEvent(l, event)

	} else {
		l.Log.Error.Printf(" Error while unmarshalling ChunkCreatedEvent %s", err)
	}
}

func processEvent(l *ChunkCreatedEventListener, event model.ChunkCreatedEvent) {
	baseDir := uuid.NewV4().String()
	os.MkdirAll(baseDir, os.ModePerm)
	defer os.RemoveAll(baseDir)
	defer os.Remove(baseDir)
	l.S3Manager.Download(event.ObjectStorageId, event.FileName, baseDir)
	files, _ := ioutil.ReadDir(baseDir)
	for _, file := range files {
		filePath := baseDir + "/" + file.Name()
		file, _ := os.Open(filePath)
		scanner := bufio.NewScanner(file)
		var header string
		var line string
		var lineNo int = 0
		defer file.Close()
		transactionRecordColl := l.MongoClient.Database(event.Tenant).Collection("transactionRecord")

		var recordsArr []interface{}

		for scanner.Scan() {
			if lineNo == 0 {
				header = scanner.Text()
			} else {
				line = scanner.Text()
			}
			if lineNo > 0 {
				record := make(map[string]interface{})
				headerTokens := strings.Split(header, "\t")
				lineTokens := strings.Split(line, "\t")
				for i, s := range headerTokens {
					h := stripRegex(s)
					var l = ""
					if i < len(lineTokens) {
						l = lineTokens[i]
						record[h] = l
					}
				}
				if len(event.IdField) == 0 {
					tr := entity.TransactionRecord{
						Name:  event.Name,
						Attrs: record,
					}
					recordsArr = append(recordsArr, tr)
				} else {
					tr := entity.TransactionRecord{
						ID:    record[event.IdField].(string),
						Name:  event.Name,
						Attrs: record,
					}
					recordsArr = append(recordsArr, tr)
				}

			}
			lineNo++
		}
		save(transactionRecordColl, l, &event, recordsArr)
	}

	updateMongo(l.MongoClient, event, "ingestedchunksize")

}

func save(transactionRecordColl *mongo.Collection, l *ChunkCreatedEventListener, event *model.ChunkCreatedEvent, tr []interface{}) {
	_, error := transactionRecordColl.InsertMany(context.TODO(), tr)
	if error != nil {
		l.Log.Error.Printf("Error during insert transactionRecord: %s", error)
	} else {

	}
}

func stripRegex(in string) string {
	reg, _ := regexp.Compile("[^a-zA-Z0-9]+")
	return reg.ReplaceAllString(in, "")
}

func updateMongo(mongo *mongo.Client, event model.ChunkCreatedEvent, field string) {
	ingestJobColl := mongo.Database(event.Tenant).Collection("ingestJob")
	ingestJobColl.UpdateOne(context.TODO(), bson.M{
		"jobid": event.JobId,
	}, bson.D{
		{Key: "$inc", Value: bson.D{{Key: field, Value: 1}}},
	}, options.Update().SetUpsert(false))
}
