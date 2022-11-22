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
	uploader := manager.NewUploader(awsS3Client)
	file, _ := os.Open(localFilePath)

	uploader.Upload(context.TODO(), &s3.PutObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(folder),
		Body:   bufio.NewReader(file),
	})
}

func NewS3Manager(log *model.Log) *S3Manager {
	return &S3Manager{log}
}
