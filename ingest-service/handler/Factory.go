package handler

import (
	"database/sql"

	_ "github.com/lib/pq"
	"github.com/nats-io/nats.go"
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
	Nats          *nats.Conn
	DB            *sql.DB
}
