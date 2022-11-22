package model

type IngestResponse struct {
	Name   string   `json:"name"`
	Tenant string   `json:"tenant"`
	Chunks []string `json:"chunks"`
}
