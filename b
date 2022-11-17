#!/bin/bash

fastbuild () {
  mvn clean install -Ddocker.image.publish=false -Ddocker.registry.password=dummy -DskipTests
}

build () {
  mvn clean install -Ddocker.image.publish=false -Ddocker.registry.password=dummy
}

buildandpush () {
  if [ -z "$2" ]
    then
      echo -e "\033[0;31m[ERROR] Supply the docker hub password as second argument to this script. Eg: ./build buildandpush <my_password>"
      exit 1
    fi
  mvn clean install -Ddocker.image.publish=true -Ddocker.registry.password=$2
}

if [ -z "$1" ]
  then
    echo -e "\033[0;31m[ERROR] Supply one of the following as an argument to the script"
    declare -pF | awk '{print $NF}'
fi

$1 $@
