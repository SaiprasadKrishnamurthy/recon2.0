package listener

import (
	"github.com/nats-io/nats.go"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/saiprasadkrishnamurthy/ingest-service/storage"
	"github.com/spf13/viper"
	"go.mongodb.org/mongo-driver/mongo"
)

// BaseListener base listener.
type BaseListener struct {
	NatsConn    *nats.Conn
	Log         *model.Log
	S3Manager   *storage.S3Manager
	MongoClient *mongo.Client
}

// OnMessage on message function of all the listeners.
func (l *BaseListener) OnMessage(subject string, queue string, msgHandler nats.MsgHandler) {
	l.NatsConn.QueueSubscribe(subject, queue, msgHandler)
}

// InitializeAllListeners initializes all listeners.
func InitializeAllListeners(nats *nats.Conn, log *model.Log, mongo *mongo.Client, s3 *storage.S3Manager) {
	baseListener := &BaseListener{NatsConn: nats, Log: log, S3Manager: s3, MongoClient: mongo}
	initChunkCreatedEventListener(baseListener)
}

func initChunkCreatedEventListener(baseListener *BaseListener) {
	l := &ChunkCreatedEventListener{BaseListener: baseListener}
	natsSubject := viper.GetString("chunk_created_event_subject")
	natsQueue := viper.GetString("chunk_created_event_queue")
	l.OnMessage(natsSubject, natsQueue, l.Handle)
	baseListener.Log.Info.Println(" initChunkCreatedEventListener Done")
}
