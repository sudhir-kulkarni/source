package com.persistent.bcsuite.beans;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.persistent.bcsuite.support.TasksMappingAdapter;

@XmlRootElement(name="subscriber-settings")
public class SubscriberSettings {
   private List<SubscriberGroup> subscriberGroups;
   
   private String dbURL;
   
   private Map<String, String> tasks;
 
   @XmlElement(name = "tasks")
   @XmlJavaTypeAdapter(value = TasksMappingAdapter.class)
   public void setTasks(Map<String, String> tasks) {
      this.tasks = tasks;
   }

   public List<SubscriberGroup> getSubscriberGroups() {
      return subscriberGroups;
   }

   @XmlElementWrapper(name="subscriber-groups")
   @XmlElement(name="group")
   public void setSubscriberGroups(List<SubscriberGroup> subscriberGroups) {
      this.subscriberGroups = subscriberGroups;
   }

   public Map<String, String> getTasks() {
      return tasks;
   }

   public String getDbURL() {
      return dbURL;
   }

   @XmlElement(name = "db-url")
   public void setDbURL(String dbURL) {
      this.dbURL = dbURL;
   }
   
   
   
}
