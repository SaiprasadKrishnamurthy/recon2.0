package model

type ChunkCreatedEvent struct {
	IdField         string          `json:"idfield"`
	JobId           string          `json:"jobid"`
	Tenant          string          `json:"tenant"`
	ObjectStorageId string          `json:"objectstorageid"`
	FileName        string          `json:"filename"`
	Name            string          `json:"name"`
	Tags            []string        `json:"tags"`
	DataDefinitions DataDefinitions `json:"datadefinitions"`
}
