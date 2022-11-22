package service

import (
	"bufio"
	"io/ioutil"
	"log"
	"os"
	"strconv"

	"github.com/saiprasadkrishnamurthy/ingest-service/util"
	"github.com/spf13/viper"
)

type IngestService struct {
}

func (s *IngestService) PartitionFiles(sourceZipFile string, baseDir string, chunkDir string) []string {
	util.Unzip(sourceZipFile, baseDir)
	unzippedFiles, err := ioutil.ReadDir(baseDir)
	if err != nil {
		log.Fatal(err)
	}
	for _, file := range unzippedFiles {
		unzippedFile := baseDir + "/" + file.Name()
		split(unzippedFile, viper.GetInt("no_of_lines_in_chunk"), chunkDir)
	}

	var chunks []string
	chunkedFiles, err := ioutil.ReadDir(chunkDir)
	if err != nil {
		log.Fatal(err)
	}
	for _, chunk := range chunkedFiles {
		chunks = append(chunks, chunkDir+"/"+chunk.Name())
	}
	return chunks
}

func split(path string, noOfLines int, outDir string) {
	file, err := os.Open(path)
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	var lineNo int = 0
	var header string
	var headerParsed bool = false
	var chunkNo = 1
	var outFile, _ = os.Create(outDir + "/chunk_" + strconv.Itoa(chunkNo) + ".txt")
	var w = bufio.NewWriter(outFile)
	defer outFile.Close()

	for scanner.Scan() {
		var line string
		if !headerParsed {
			header = scanner.Text()
			w.WriteString(header + "\n")
			headerParsed = true
		} else {
			line = scanner.Text()
			w.WriteString(line + "\n")
		}
		lineNo++

		// Split point.
		if lineNo%noOfLines == 0 {
			lineNo = 0
			chunkNo++
			outFile, _ = os.Create(outDir + "/chunk_" + strconv.Itoa(chunkNo) + ".txt")
			w.Flush()
			w = bufio.NewWriter(outFile)
			w.WriteString(header + "\n")
		}
	}

	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}
}

func NewIngestService() *IngestService {
	return &IngestService{}
}
