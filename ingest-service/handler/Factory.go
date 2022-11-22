package handler

import (
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/saiprasadkrishnamurthy/ingest-service/service"
	"github.com/saiprasadkrishnamurthy/ingest-service/storage"
	"go.mongodb.org/mongo-driver/mongo"
)

type ServiceFactory struct {
	IngestService *service.IngestService
	Log           *model.Log
	MongoClient   *mongo.Client
	S3Manager     *storage.S3Manager
}
