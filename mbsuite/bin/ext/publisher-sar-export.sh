#!/bin/bash


export CLASSPATH=$CLASSPATH:../../ext-lib/
token=$1
interval=$3
runForSec=$4
APIName=$2
fileName=$APIName-publisher-
if [[ $1 = "" || $2 = "" || $3 = "" || $4 = "" ]]; then
echo "No parameter supplied valid sequence is <./sar-export.sh> <token> <API_Name> <interval> <runForSec> "
else
echo 'publisher sar utility started... '
sar -o ../../export/data-files/$fileName$token.sar -A $interval $runForSec


LC_ALL=C sar -A -f  ../../export/data-files/$fileName$token.sar >  ../../export/data-files/$fileName$token.txt

if [ ! -d  ../../export/ksar/$token/publisher/images ]; then
    mkdir -p  ../../export/ksar/$token/publisher/images
fi


java -jar ../../ext-lib/kSar.jar -input ../../export/data-files/$fileName$token.txt -outputJPG ../../export/ksar/$token/publisher/images/export_img.jpeg > ../../export/data-files/tmp.txt  2>&1
echo 'publisher sar utility finished...'
fi