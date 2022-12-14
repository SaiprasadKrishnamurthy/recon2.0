package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
	"time"

	"database/sql"

	"github.com/gin-gonic/gin"
	_ "github.com/lib/pq"
	"github.com/nats-io/nats.go"
	"github.com/saiprasadkrishnamurthy/ingest-service/handler"
	"github.com/saiprasadkrishnamurthy/ingest-service/listener"
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
	os.Mkdir("logs", os.ModePerm)
	logFile, _ = os.OpenFile("logs/ingest-service-logs.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0666)
	fmt.Println(" Setting up log file ", logFile.Name())
}

func main() {
	r := gin.Default()
	readConfig()

	// Postgres DB
	host := viper.GetString("db_host")
	port := viper.GetInt32("db_port")
	user := viper.GetString("db_user")
	password := viper.GetString("db_password")
	dbname := viper.GetString("db_dbname")
	psqlconn := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable", host, port, user, password, dbname)
	db, err := sql.Open("postgres", psqlconn)
	CheckError(err)

	// close database
	defer db.Close()

	// check db
	err = db.Ping()
	CheckError(err)
	serviceFactory := setupDependencies(db)
	apiVersion := viper.GetString("api_version")
	serviceName := viper.GetString("service_name")
	r.Use(middleware.Auth(serviceFactory.MongoClient))
	r.Use(middleware.CORSMiddleware())
	r.POST(serviceName+"/api/"+apiVersion+"/ingest/:name/", func(ctx *gin.Context) {
		form, _ := ctx.MultipartForm()
		name := ctx.Param("name")
		tags := ctx.Query("tags")
		file, _ := ctx.FormFile("file")
		dataDefinitions := strings.ReplaceAll(form.Value["dataDefinitions"][0], "\\r\\n", "")

		var def model.DataDefinitions
		e := json.Unmarshal([]byte(dataDefinitions), &def)

		if e != nil {
			ctx.JSON(400, model.PartitionFileResponse{Errors: []model.ApiError{{Code: "ING0001", Message: "Invalid data definition"}}})
		} else {
			destDir := uuid.NewV4().String()
			defer os.RemoveAll(destDir)
			defer os.Remove(destDir)

			if err := os.Mkdir(destDir, os.ModePerm); err != nil {
				log.Fatal(err)
			}

			err = ctx.SaveUploadedFile(file, destDir+"/"+file.Filename)

			if err != nil {
				fmt.Println("Cant save the uploaded file to disk! ", err)
			}

			tenant, _ := ctx.Get("tenant")
			objectStorageId, _ := ctx.Get("objectStorageId")
			response := serviceFactory.HandlePartition(
				&model.PartitionFileRequest{
					Tenant:          tenant.(string),
					ObjectStorageId: objectStorageId.(string),
					Name:            name,
					Tags:            strings.Split(tags, ","),
					ZipFilePath:     destDir + "/" + file.Filename,
					DataDefinitions: def,
				})
			ctx.JSON(200, response)
		}
	})
	r.Run(":" + strconv.Itoa(viper.GetInt("server_port")))
}

func setupDependencies(db *sql.DB) *handler.ServiceFactory {
	log := &model.Log{
		Info:  log.New(logFile, "INFO: ", log.Ldate|log.Ltime|log.Lshortfile),
		Warn:  log.New(logFile, "WARNING: ", log.Ldate|log.Ltime|log.Lshortfile),
		Error: log.New(logFile, "ERROR: ", log.Ldate|log.Ltime|log.Lshortfile),
	}
	fmt.Println(" Log File ===> ", logFile.Name())
	return NewServiceFactory(log,
		service.NewIngestService(log),
		storage.NewS3Manager(log),
		db,
	)
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
	viper.AddConfigPath("./conf/") // config file path
	viper.AutomaticEnv()           // read value ENV variable
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	err := viper.ReadInConfig()
	if err != nil {
		fmt.Println("fatal error config file \n", err)
		os.Exit(1)
	}
	fmt.Printf("Profile: %s\n", viper.GetString("profile"))
}
func NewServiceFactory(log *model.Log,
	ingestService *service.IngestService,
	s3Manager *storage.S3Manager,
	db *sql.DB) *handler.ServiceFactory {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	client, err := mongo.Connect(ctx, options.Client().ApplyURI(viper.GetString("mongo_uri")))
	if err != nil {
		fmt.Println("fatal error connecting to mongo \n", err)
		os.Exit(1)
	}

	nc, _ := nats.Connect(viper.GetString("nats_url"))
	js, _ := nc.JetStream()

	x, err := js.AddStream(&nats.StreamConfig{
		Name:     viper.GetString("ingest_stream"),
		Subjects: []string{viper.GetString("ingest_event_subject")},
	})

	if err != nil {
		log.Warn.Printf(" Unable to create a stream %s", err)
	} else {
		log.Info.Printf(" Created a stream %s with a subject %s at %s",
			viper.GetString("ingest_stream"),
			viper.GetString("ingest_event_subject"), x.Created)
	}

	listener.InitializeAllListeners(nc, log, client, s3Manager, db)
	return &handler.ServiceFactory{
		IngestService: ingestService,
		MongoClient:   client,
		Log:           log,
		S3Manager:     s3Manager,
		Nats:          nc,
		DB:            db,
	}

}
func CheckError(err error) {
	if err != nil {
		panic(err)
	}
}
