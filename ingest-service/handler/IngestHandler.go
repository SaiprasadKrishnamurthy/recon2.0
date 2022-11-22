package handler

import (
	"log"
	"os"
	"strings"

	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/twinj/uuid"
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

	for _, chunkFile := range chunks {
		serviceFactory.Log.Info.Println(" About to upload to S3", chunkFile)
		id := strings.Split(chunkFile, "/")[0]
		fileName := strings.Split(chunkFile, "/")[1]
		s3Key := request.Name + "/" + id + "/" + fileName
		serviceFactory.S3Manager.UploadToS3(request.ObjectStorageId, s3Key, chunkFile)
		serviceFactory.Log.Info.Printf(" Uploaded %s to S3 to %s", chunkFile, s3Key)
	}

	// Upload these Files to S3.
	// Emit an Event per file with uploaded file path.
	// Save a job record in DB with expected chunk size.
	// Send Response.

	return model.PartitionFileResponse{Name: request.Name,
		Tenant: request.Tenant,
		Chunks: chunks}

}
