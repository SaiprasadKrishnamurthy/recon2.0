package entity

import "go.mongodb.org/mongo-driver/bson/primitive"

type ApiUser struct {
	ID              primitive.ObjectID `bson:"_id,omitempty"`
	Email           string             `bson:"email,omitempty"`
	Company         string             `bson:"company,omitempty"`
	ClientId        string             `bson:"clientid,omitempty"`
	ApiKey          string             `bson:"apikey,omitempty"`
	TenantId        string             `bson:"tenantid,omitempty"`
	ObjectStorageId string             `bson:"objectstorageid,omitempty"`
	Active          bool               `bson:"active,omitempty"`
}
