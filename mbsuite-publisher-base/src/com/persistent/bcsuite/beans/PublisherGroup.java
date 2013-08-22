package com.persistent.bcsuite.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.persistent.bcsuite.support.AttributesMappingAdapter;

/**
 * This class holds the information provided in the configuration xml file. JAXB is used to convert xml data.
 * 
 */
@XmlRootElement(name = "group")
public class PublisherGroup {
   private String name;
   private int totalPublishers;
   private boolean enableMaxThroughput;
   private int thinkTimeInMillis;
   private int warmUpTimeInMillis;
   private int messageSize;
   private Map<String, String> attributes;
   private String loadDistStrategy = "balanced";
   private int runForSeconds = 5;
   private boolean dumpDetails = false;
   private int numTopics;
   private String iterationType="cold";
   private int iterationDrainTimeInMillis;
   private int iterationsToIgnore=0;
   public String getName() {
      return name;
   }

   @XmlAttribute
   public void setName(String name) {
      this.name = name;
   }

   public int getTotalPublishers() {
      return totalPublishers;
   }

   @XmlElement(name = "total-publishers")
   public void setTotalPublishers(int totalPublishers) {
      this.totalPublishers = totalPublishers;
   }

   public boolean isEnableMaxThroughput() {
      return enableMaxThroughput;
   }

   public void setEnableMaxThroughput(boolean enableMaxThroughput) {
      this.enableMaxThroughput = enableMaxThroughput;
   }

   public int getThinkTimeInMillis() {
      return thinkTimeInMillis;
   }

   @XmlElement(name = "think-time-in-ms")
   public void setThinkTimeInMillis(int thinkTimeInMillis) {
      this.thinkTimeInMillis = thinkTimeInMillis;
   }

   public int getWarmUpTimeInMillis() {
      return warmUpTimeInMillis;
   }

   @XmlElement(name = "warm-up-time-in-ms")
   public void setWarmUpTimeInMillis(int warmUpTimeInMillis) {
      this.warmUpTimeInMillis = warmUpTimeInMillis;
   }

   public int getMessageSize() {
      return messageSize;
   }

   @XmlElement(name = "message-size")
   public void setMessageSize(int messageSize) {
      this.messageSize = messageSize;
   }

   public Map<String, String> getAttributes() {
      return attributes;
   }

   @XmlElement(name = "attributes")
   @XmlJavaTypeAdapter(value = AttributesMappingAdapter.class)
   public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
   }

   public String getLoadDistStrategy() {
      return loadDistStrategy;
   }

   @XmlElement(name = "load-dist-strategy")
   public void setLoadDistStrategy(String loadDistStrategy) {
      this.loadDistStrategy = loadDistStrategy;
   }

   public int getRunForSeconds() {
      return runForSeconds;
   }

   @XmlElement(name = "run-for-secs")
   public void setRunForSeconds(int runForSeconds) {
      this.runForSeconds = runForSeconds;
   }

   public boolean isDumpDetails() {
      return dumpDetails;
   }

   @XmlElement(name = "dump-details")
   public void setDumpDetails(boolean dumpDetails) {
      this.dumpDetails = dumpDetails;
   }

   public int getNumTopics() {
      return numTopics;
   }

   @XmlElement(name = "num-topics")
   public void setNumTopics(int numTopics) {
      this.numTopics = numTopics;
   }

   public String getIterationType() {
      return iterationType;
   }

   @XmlElement(name = "iteration-type")
   public void setIterationType(String iterationType) {
      this.iterationType = iterationType;
   }

   public int getIterationDrainTimeInMillis() {
      return iterationDrainTimeInMillis;
   }

   @XmlElement(name = "iteration-drain-time-in-ms")
   public void setIterationDrainTimeInMillis(int iterationDrainTimeInMillis) {
      this.iterationDrainTimeInMillis = iterationDrainTimeInMillis;
   }

   public int getIterationsToIgnore() {
      return iterationsToIgnore;
   }

   @XmlElement(name = "iterations-to-ignore")
   public void setIterationsToIgnore(int iterationsToIgnore) {
      this.iterationsToIgnore = iterationsToIgnore;
   }

   
   

}
