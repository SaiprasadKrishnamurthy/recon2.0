package entity

import "go.mongodb.org/mongo-driver/bson/primitive"

type IngestJob struct {
	ID                primitive.ObjectID `bson:"_id,omitempty"`
	JobId             string             `bson:"jobid,omitempty"`
	Started           primitive.DateTime `bson:"started,omitempty"`
	Ended             primitive.DateTime `bson:"clientid,omitempty"`
	ExpectedChunkSize int                `bson:"expectedchunksize,omitempty"`
	QueuedChunkSize   int                `bson:"queuedchunksize,omitempty"`
	ReceivedChunkSize int                `bson:"receivedchunksize,omitempty"`
	IngestedChunkSize int                `bson:"ingestedchunksize,omitempty"`
	Error             string             `bson:"error,omitempty"`
}
