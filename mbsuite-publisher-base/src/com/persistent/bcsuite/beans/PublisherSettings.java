package com.persistent.bcsuite.beans;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.persistent.bcsuite.support.TasksMappingAdapter;
/**
 * This class holds the publisher settings data provided in the configuration xml file
 * 
 * JAXB is used to convert the xml to java object.
 *
 */
@XmlRootElement(name="publisher-settings")
public class PublisherSettings {
   
   private List<PublisherGroup> publisherGroups;
   private Map<String, String> tasks;

   private String dbURL;
   
   public List<PublisherGroup> getPublisherGroups() {
      return publisherGroups;
   }
   
   @XmlElementWrapper(name="publisher-groups")
   @XmlElement(name="group")
   public void setPublisherGroups(List<PublisherGroup> publisherGroups) {
      this.publisherGroups = publisherGroups;
   }
      
   public Map<String, String> getTasks() {
      return tasks;
   }

   @XmlElement(name = "tasks")
   @XmlJavaTypeAdapter(value = TasksMappingAdapter.class)
   public void setTasks(Map<String, String> tasks) {
      this.tasks = tasks;
   }

   public String getDbURL() {
      return dbURL;
   }

   @XmlElement(name = "db-url")
   public void setDbURL(String dbURL) {
      this.dbURL = dbURL;
   }

}
