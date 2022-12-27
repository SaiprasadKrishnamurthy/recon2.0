package model

type PartitionFileRequest struct {
	Tenant          string
	ObjectStorageId string
	Name            string
	ZipFilePath     string
	Tags            []string
	DataDefinitions DataDefinitions
}

type DataDefinitions struct {
	IdField     string              `json:"idfield"`
	KeyFields   []string            `json:"keyfields"`
	Definitions map[string]DataType `json:"definitions"`
}

type DataType string

const (
	String  DataType = "string"
	Text    DataType = "text"
	Number  DataType = "number"
	Date    DataType = "date"
	Boolean DataType = "boolean"
)

type PartitionFileResponse struct {
	Name   string     `json:"name"`
	Tenant string     `json:"tenant"`
	Chunks []string   `json:"chunks"`
	Errors []ApiError `json:"errors,omitempty"`
}
