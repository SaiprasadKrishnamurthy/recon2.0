package handler

import (
	"github.com/saiprasadkrishnamurthy/ingest-service/service"
	"go.mongodb.org/mongo-driver/mongo"
)

type ServiceFactory struct {
	IngestService *service.IngestService
	MongoClient   *mongo.Client
}
