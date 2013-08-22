package com.persistent.bcsuite.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.persistent.bcsuite.support.TopicsMappingAdapter;


@XmlRootElement(name = "group")
public class SubscriberGroup {
   
   public final static String EQUAL_DISTRIBUTION = "balanced";
   
   private String name;
   private int totalSubscribers;
   private String subscriberDistribution;
   private int subscriberShutdownDelay;
   
   private int warmUpTimeInMillis=0;
   private boolean dumpDetails=false;

   private int numTopics;
   private Map<String,String> topicAttributes;
   
   private String iterationType="cold";
   private int iterationDrainTimeInMillis;
   private int iterationsToIgnore=0;
   
   public int getSubscriberShutdownDelay() {
      return subscriberShutdownDelay;
   }

   @XmlElement(name="subscriber-shutdown-after-secs")
   public void setSubscriberShutdownDelay(int subscriberShutdownDelay) {
      this.subscriberShutdownDelay = subscriberShutdownDelay;
   }

   public int getTotalSubscribers() {
      return totalSubscribers;
   }

   @XmlElement(name="total-subscribers")
   public void setTotalSubscribers(int totalSubscribers) {
      this.totalSubscribers = totalSubscribers;
   }

   public String getSubscriberDistribution() {
      return subscriberDistribution;
   }

   @XmlElement(name="subscriber-distribution")
   public void setSubscriberDistribution(String subscriberDistribution) {
      this.subscriberDistribution = subscriberDistribution;
   }
   
   public String getName() {
      return name;
   }

   @XmlAttribute
   public void setName(String name) {
      this.name = name;
   }

   public int getWarmUpTimeInMillis() {
      return warmUpTimeInMillis;
   }
   @XmlElement(name = "warm-up-time-in-ms")
   public void setWarmUpTimeInMillis(int warmUpTimeInMillis) {
      this.warmUpTimeInMillis = warmUpTimeInMillis;
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

   @XmlElement(name = "topic-attributes")
   @XmlJavaTypeAdapter(value = TopicsMappingAdapter.class)
   public void setAttributes(Map<String, String> attributes) {
      this.topicAttributes = attributes;
   }

   public Map<String, String> getAttributes() {
      return topicAttributes;
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
