package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/saiprasadkrishnamurthy/ingest-service/handler"
	"github.com/saiprasadkrishnamurthy/ingest-service/middleware"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/saiprasadkrishnamurthy/ingest-service/service"
	"github.com/saiprasadkrishnamurthy/ingest-service/storage"
	"github.com/spf13/viper"
	"github.com/twinj/uuid"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var logFile *os.File

func init() {
	logFile, _ = os.OpenFile("ingest-service-logs.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0666)
	fmt.Println(" Setting up log file ", logFile.Name())
}

func main() {
	r := gin.Default()
	serviceFactory := setupDependencies()
	apiVersion := viper.GetString("api_version")
	serviceName := viper.GetString("service_name")
	r.Use(middleware.Auth(serviceFactory.MongoClient))
	r.Use(middleware.CORSMiddleware())
	r.POST(serviceName+"/api/"+apiVersion+"/ingest/:name", func(ctx *gin.Context) {
		name := ctx.Param("name")
		idField := ctx.Query("idField")
		tags := ctx.Query("tags")
		file, _ := ctx.FormFile("file")
		destDir := uuid.NewV4().String()
		defer os.RemoveAll(destDir)
		defer os.Remove(destDir)

		if err := os.Mkdir(destDir, os.ModePerm); err != nil {
			log.Fatal(err)
		}

		err := ctx.SaveUploadedFile(file, destDir+"/"+file.Filename)

		if err != nil {
			fmt.Println("Cant save the uploaded file to disk! ", err)
		}

		tenant, _ := ctx.Get("tenant")
		objectStorageId, _ := ctx.Get("objectStorageId")
		response := serviceFactory.HandlePartition(
			&model.PartitionFileRequest{
				IdField:         idField,
				Tenant:          tenant.(string),
				ObjectStorageId: objectStorageId.(string),
				Name:            name,
				Tags:            strings.Split(tags, ","),
				ZipFilePath:     destDir + "/" + file.Filename,
			})
		ctx.JSON(200, response)
	})
	r.Run(":" + strconv.Itoa(viper.GetInt("server_port")))
}

func setupDependencies() *handler.ServiceFactory {
	readConfig()
	log := &model.Log{
		Info:  log.New(logFile, "INFO: ", log.Ldate|log.Ltime|log.Lshortfile),
		Warn:  log.New(logFile, "WARNING: ", log.Ldate|log.Ltime|log.Lshortfile),
		Error: log.New(logFile, "ERROR: ", log.Ldate|log.Ltime|log.Lshortfile),
	}
	fmt.Println(" Log File ===> ", logFile.Name())
	return NewServiceFactory(log,
		service.NewIngestService(log),
		storage.NewS3Manager(log))
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
func NewServiceFactory(log *model.Log,
	ingestService *service.IngestService,
	s3Manager *storage.S3Manager) *handler.ServiceFactory {
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
		Log:           log,
		S3Manager:     s3Manager,
	}
}
