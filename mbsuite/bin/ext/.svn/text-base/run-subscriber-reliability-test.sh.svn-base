#!/bin/bash

#load the parameters to run the specific test from  config/subscriber-config.properties 
. config/subscriber-config.properties 

#parameter for subscriber
key=$1
groupName=$2
token=$3


#check if all parameters are provided for test
if [[ $1 = "" || $2 = "" || $3 = ""  ]]; then
echo "No parameter supplied :  valid sequence is<./run-subscriber-reliability-test.sh> <key> <groupName> <token>"
else

 #copy the  test specific config parameter values from "subscriber-config.tmpl" to  "subscriber-config.xml"
 cp ../../config/subscriber-config.tmpl ../../config/subscriber-config.xml
 sed -i 's/@shutdownDelay/'$shutdownDelay'/g' ../../config/subscriber-config.xml 
 sed -i 's/@dumpDetails/'$dumpDetails'/g'  ../../config/subscriber-config.xml 
 sed -i 's/@maxSubscriber/'$maxSubscriber'/g' ../../config/subscriber-config.xml 
 sed -i 's/@maxTopics/'$maxTopics'/g' ../../config/subscriber-config.xml 
 
#run the extractor utility which actually receives the messages.
#loads the external jars from "ext-lib" , utility jars from "lib" & configuration parameter from .tmpl file inside  "config" dir 
  echo "Receiving messages..runfor[$iteration times] " 
   java -d64 -Xms4048M -Xmx14048M  -cp   "../../ext-lib/*":"../../ext-lib/rti/*":"../../export/data-files/":"../../lib/mbsuite-addons.jar":"../../config/":"../../lib/mbsuite-subscriber-base.jar":"../../export/"   com.persistent.bcsuite.process.Extractor $key $groupName $token $iteration >> logs/run-subscriber-reliability-test.log  
 

fi



