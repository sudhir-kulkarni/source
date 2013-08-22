package com.persistent.bcsuite.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.persistent.bcsuite.support.TopicsMappingAdapter;

@XmlRootElement(name="topic")
public class Topic {
   private String name;
   

   
   public String getName() {
      return name;
   }
   @XmlAttribute
   public void setName(String name) {
      this.name = name;
   }

 
}
