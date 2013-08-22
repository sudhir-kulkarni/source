#!/bin/bash
deviceName=$4
token=$2
interval=$3
runForSec=$1
if [[ $1 = "" || $2 = "" || $3 = "" || $4 = "" ]]; then
echo "No parameter supplied valid sequence is <./filename>  <runForSec> <token> <interval> <deviceName>"
else
#echo "Gathering Network Statistics..."
nicstat.sh -p  $interval $runForSec > ../../export/data-files/$deviceName.txt 
#echo "Gathering Network statistics done..."
./device-statistics-to-db.sh $deviceName $token 
fi