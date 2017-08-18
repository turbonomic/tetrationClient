#!/bin/bash

host="https://perseus-aus.cisco.com"
apiKey=""
apiSecret=""
printHeader=true
connTimeOut=1000
readTimeOut=60000

myjar=target/TetrationClient-1.0-SNAPSHOT-jar-with-dependencies.jar
mymain=com.turbo.tetration.App

mvn package
ret=$?
if [ $ret -ne 0 ] ; then
    echo "build failed"
    exit 1
fi

java -cp $myjar $mymain $host $apiKey $apiSecret $printHeader $connTimeOut $readTimeOut


