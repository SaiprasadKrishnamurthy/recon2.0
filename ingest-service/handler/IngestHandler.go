package handler

import (
	"log"
	"os"

	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/twinj/uuid"
)

func (serviceFactory *ServiceFactory) HandlePartition(request *model.PartitionFileRequest) model.PartitionFileResponse {
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

	chunks := serviceFactory.IngestService.PartitionFiles(request.ZipFilePath, baseDir, chunkDir)

	// Upload these Files to S3.
	// Emit an Event per file with uploaded file path.
	// Save a job record in DB with expected chunk size.
	// Send Response.

	return model.PartitionFileResponse{Name: request.Name,
		Tenant: request.Tenant,
		Chunks: chunks}

}
