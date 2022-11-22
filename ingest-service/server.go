package main

import (
	"context"
	"fmt"
	"os"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/saiprasadkrishnamurthy/ingest-service/handler"
	"github.com/saiprasadkrishnamurthy/ingest-service/middleware"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/saiprasadkrishnamurthy/ingest-service/service"
	"github.com/spf13/viper"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func main() {
	r := gin.Default()
	serviceFactory := setupDependencies()
	r.Use(middleware.Auth(serviceFactory.MongoClient))
	r.Use(middleware.CORSMiddleware())
	r.GET("/", func(ctx *gin.Context) {
		tenant, _ := ctx.Get("tenant")
		response := serviceFactory.HandlePartition(
			&model.PartitionFileRequest{
				IdField:     "",
				Tenant:      tenant.(string),
				Name:        "sales",
				ZipFilePath: "/Users/saiprasadkrishnamurthy/2.0/recon2.0/ingest-service/ingest.zip",
			})
		ctx.JSON(200, response)
	})
	r.Run(":38080")
}

func setupDependencies() *handler.ServiceFactory {
	readConfig()
	return NewServiceFactory(service.NewIngestService())
}

func readConfig() {
	var env = os.Getenv("ENV")
	if len(env) == 0 {
		env = "default"
	}
	fmt.Println(" Using Env : ", env)
	viper.SetConfigName(env) // config file name without extension
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	viper.AddConfigPath("./config/") // config file path
	viper.AutomaticEnv()             // read value ENV variable
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	err := viper.ReadInConfig()
	if err != nil {
		fmt.Println("fatal error config file \n", err)
		os.Exit(1)
	}
}
func NewServiceFactory(ingestService *service.IngestService) *handler.ServiceFactory {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	client, err := mongo.Connect(ctx, options.Client().ApplyURI(viper.GetString("mongo_uri")))
	if err != nil {
		fmt.Println("fatal error connecting to mongo \n", err)
		os.Exit(1)
	}
	return &handler.ServiceFactory{
		IngestService: ingestService,
		MongoClient:   client,
	}
}
