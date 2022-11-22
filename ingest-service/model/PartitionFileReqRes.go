package model

type PartitionFileRequest struct {
	IdField     string
	Tenant      string
	Name        string
	ZipFilePath string
	Tags        []string
}

type PartitionFileResponse struct {
	Name   string
	Tenant string
	Chunks []string
}
