package listener

import (
	"bufio"
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"reflect"
	"regexp"
	"sort"
	"strings"

	"github.com/nats-io/nats.go"
	"github.com/saiprasadkrishnamurthy/ingest-service/model"
	"github.com/twinj/uuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type ChunkCreatedEventListener struct {
	*BaseListener
	Log *model.Log
}

func (l *ChunkCreatedEventListener) Handle(msg *nats.Msg) {
	var event model.ChunkCreatedEvent
	err := json.Unmarshal(msg.Data, &event)

	if err == nil {
		updateMongo(l.MongoClient, event, "receivedchunksize")
		processEvent(l, event)

	} else {
		l.Log.Error.Printf(" Error while unmarshalling ChunkCreatedEvent %s", err)
	}
}

func processEvent(l *ChunkCreatedEventListener, event model.ChunkCreatedEvent) {
	baseDir := uuid.NewV4().String()
	os.MkdirAll(baseDir, os.ModePerm)
	defer os.RemoveAll(baseDir)
	defer os.Remove(baseDir)
	l.S3Manager.Download(event.ObjectStorageId, event.FileName, baseDir)
	files, _ := ioutil.ReadDir(baseDir)
	for _, file := range files {
		filePath := baseDir + "/" + file.Name()
		file, _ := os.Open(filePath)
		scanner := bufio.NewScanner(file)
		var header string
		var line string
		var lineNo int = 0
		defer file.Close()

		var recordsArr []interface{}

		for scanner.Scan() {
			if lineNo == 0 {
				header = scanner.Text()
			} else {
				line = scanner.Text()
			}
			if lineNo > 0 {
				record := make(map[string]interface{})
				headerTokens := strings.Split(header, "\t")
				lineTokens := strings.Split(line, "\t")
				for i, s := range headerTokens {
					h := stripRegex(s)
					var l = ""
					if i < len(lineTokens) {
						l = lineTokens[i]
						record[h] = l
					}
				}
				recordsArr = append(recordsArr, record)
			}
			lineNo++

		}
		insertRows(l.DB, &event, context.TODO(), recordsArr)

	}

	updateMongo(l.MongoClient, event, "ingestedchunksize")

}

func stripRegex(in string) string {
	reg, _ := regexp.Compile("[^a-zA-Z0-9]+")
	return reg.ReplaceAllString(in, "")
}

func updateMongo(mongo *mongo.Client, event model.ChunkCreatedEvent, field string) {
	ingestJobColl := mongo.Database(event.Tenant).Collection("ingestJob")
	ingestJobColl.UpdateOne(context.TODO(), bson.M{
		"jobid": event.JobId,
	}, bson.D{
		{Key: "$inc", Value: bson.D{{Key: field, Value: 1}}},
	}, options.Update().SetUpsert(false))
}

func insertRows(db *sql.DB, event *model.ChunkCreatedEvent, ctx context.Context, recordsArr []interface{}) error {
	var sql = ""
	mappedDefinitions := make([]string, len(event.DataDefinitions.Definitions))

	for k, _ := range event.DataDefinitions.Definitions {
		s := normaliseString(k)
		fmt.Println("====> ", s, len(s))
		if len(strings.TrimSpace(s)) > 0 {
			mappedDefinitions = append(mappedDefinitions, normaliseString(k))
		}
	}
	fmt.Println(len(mappedDefinitions))
	for index, w := range recordsArr {
		iter := reflect.ValueOf(w).MapRange()
		rec := make(map[string]string)

		for iter.Next() {
			key := iter.Key().Interface()
			value := iter.Value().Interface()
			col := normaliseString(fmt.Sprintf("%s", key))
			colValue := fmt.Sprintf("%s", value)
			rec[col] = strings.ReplaceAll(colValue, "'", "")
		}
		keys := make([]string, 0, len(rec))
		for k := range rec {
			if contains(mappedDefinitions, k) {
				keys = append(keys, k)
			}
		}
		sort.Strings(keys)

		// if len(keys) != len(mappedDefinitions) {
		//fmt.Println(" !!!!! ", keys, mappedDefinitions)
		// }
		namedCols := strings.Join(keys[:], ",")
		if index == 0 {
			sql += fmt.Sprintf("INSERT INTO %s.%s(%s) VALUES ", event.Tenant, event.Name, namedCols)
		} else {
			sql += "("
			for _, key := range keys {
				sql += "'" + rec[key] + "',"
			}
			sql = strings.TrimSuffix(sql, ",")
			sql += "),"
		}
	}
	sql = strings.TrimSuffix(sql, ",")
	tx, err := db.Begin()
	fmt.Println(" Begin Txn SQL ", err)
	_, err = tx.Exec(sql)
	fmt.Println(" Exec SQL ", err)
	if err != nil {
		fmt.Println(" SQL ", sql)
		tx.Rollback()
		return err
	}
	err = tx.Commit()
	return err

}

func contains(s []string, str string) bool {
	for _, v := range s {
		if v == str {
			return true
		}
	}

	return false
}

var nonAlphanumericRegex = regexp.MustCompile(`[^a-zA-Z0-9]+`)

func normaliseString(str string) string {
	return strings.ToLower(nonAlphanumericRegex.ReplaceAllString(str, ""))
}
