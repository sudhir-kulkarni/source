#!/bin/bash

#load the parameters to run the specific test from  config/publisher-config.properties 
. config/publisher-config.properties 

#parameter for publisher

key=$1
groupName=$2
token=$3



#check if all parameters are provided for test
if [[ $1 = "" || $2 = "" || $3 = "" ]]; then
echo "No parameter supplied :  valid sequence is <./run-publisher-reliability-test.sh>  <key> <groupName> <token>"
else

#copy the  test specific config parameter values from "publisher-config.tmpl" to  "publisher-config.xml"
 cp ../../config/publisher-config.tmpl ../../config/publisher-config.xml
 sed -i 's/@runSec/'$runSec'/g' ../../config/publisher-config.xml 
 sed -i 's/@msgSize/'$msgSize'/g' ../../config/publisher-config.xml 
 sed -i 's/@maxPublisher/'$maxPublisher'/g' ../../config/publisher-config.xml 
 sed -i 's/@dumpDetails/'$dumpDetails'/g' ../../config/publisher-config.xml 
 sed -i 's/@maxTopics/'$maxTopics'/g' ../../config/publisher-config.xml 

#run the generator utility which actually sends the messages.
#loads the external jars from "ext-lib" , utility jars from "lib" & configuration parameter from .tmpl file inside  "config" dir 
	echo "Sending messages.. ..runfor[$iteration times] "
   java -d64 -Xms4048M -Xmx14048M  -cp   "../../ext-lib/*":"../../ext-lib/rti/*":"../../export/data-files/*":"../../lib/*":"../../config/":"../../export/" com.persistent.bcsuite.process.Generator $key $groupName $token $iteration >> logs/run-publisher-reliability-test.log 


fi



