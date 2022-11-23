package storage

import (
	"bufio"
	"context"
	"log"
	"os"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/feature/s3/manager"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/spf13/viper"
)

type S3Manager struct {
	Log *model.Log
}

// configS3 creates the S3 client
func (s *S3Manager) UploadToS3(bucket string, folder string, localFilePath string) {
	awsS3Client := s3Client()
	uploader := manager.NewUploader(awsS3Client)
	file, _ := os.Open(localFilePath)

	uploader.Upload(context.TODO(), &s3.PutObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(folder),
		Body:   bufio.NewReader(file),
	})
}

func (s *S3Manager) Download(objectStorageId string, fileName string, baseDir string) {
	awsS3Client := s3Client()
	downloader := manager.NewDownloader(awsS3Client)
	f, _ := os.Create(baseDir + "/downloaded_chunk.txt")
	downloader.Download(context.TODO(), f, &s3.GetObjectInput{
		Bucket: aws.String(objectStorageId),
		Key:    aws.String(fileName),
	})
}
func NewS3Manager(log *model.Log) *S3Manager {
	return &S3Manager{log}
}

func s3Client() *s3.Client {
	var awsS3Client *s3.Client
	creds := credentials.NewStaticCredentialsProvider(
		viper.GetString("aws_access_key_id"),
		viper.GetString("aws_secret_access_key"),
		"")
	cfg, err := config.LoadDefaultConfig(context.TODO(),
		config.WithCredentialsProvider(creds),
		config.WithRegion(viper.GetString("aws_region")))
	if err != nil {
		log.Fatal(err)
	}

	awsS3Client = s3.NewFromConfig(cfg)
	return awsS3Client
}
