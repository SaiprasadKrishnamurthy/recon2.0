#!/bin/bash

build () {
  mvn clean install -Ddocker.image.publish=false -Ddocker.registry.password=dummy -DskipTests
  echo "Building Ingest Service GO Project"
  curr=`pwd`
  rm -rf ingest-service/bin/*
  GOOS=linux GOARCH=amd64 && cd ingest-service && go build -o `pwd`/bin/ingest-service-amd64-linux . && echo "Built Linux Compatible Binary of Ingest Service GO Project" && cd ..
  GOOS=darwin GOARCH=amd64 && cd ingest-service && go build -o `pwd`/bin/ingest-service-amd64-linux . && echo "Built OS/X Compatible Binary of Ingest Service GO Project" && cd ..
  GOOS=windows GOARCH=darwin && cd ingest-service && go build -o `pwd`/bin/ingest-service-amd64-linux . && echo "Built Windows Compatible Binary of Ingest Service GO Project" && cd ..
  cd $curr
}

buildandpush () {
  if [ -z "$2" ]
    then
      echo -e "\033[0;31m[ERROR] Supply the docker hub password as second argument to this script. Eg: ./build buildandpush <my_password>"
      exit 1
    fi
  mvn clean install -Ddocker.image.publish=true -DskipTests -Ddocker.registry.password=$2
}

if [ -z "$1" ]
  then
    echo -e "\033[0;31m[ERROR] Supply one of the following as an argument to the script"
    declare -pF | awk '{print $NF}'
fi


$1 $@

./copy
