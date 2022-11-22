package handler

import "github.com/saiprasadkrishnamurthy/ingest-service/service"

type ServiceFactory struct {
	IngestService *service.IngestService
}
