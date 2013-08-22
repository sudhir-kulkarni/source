package com.persistent.bcsuite.support;

import java.util.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This is a mapping adapter class used to deserialize the xml settings to a Java object. This is particularly used to
 * convert the attributes in configuration to a Map.
 * 
 * 
 */
public class AttributesMappingAdapter extends XmlAdapter<AttributesMappingAdapter.AdaptedMap, Map<String, String>> {

   public static class AdaptedMap {

      public List<Attribute> attribute = new ArrayList<Attribute>();

   }

   public static class Attribute {
      public String key;
      public String value;
   }

   @Override
   public Map<String, String> unmarshal(AdaptedMap adaptedMap) throws Exception {
      Map<String, String> map = new HashMap<String, String>();
      for (Attribute entry : adaptedMap.attribute) {
         map.put(entry.key, entry.value);
      }
      return map;
   }

   @Override
   public AdaptedMap marshal(Map<String, String> map) throws Exception {
      AdaptedMap adaptedMap = new AdaptedMap();
      for (Map.Entry<String, String> mapEntry : map.entrySet()) {
         Attribute entry = new Attribute();
         entry.key = mapEntry.getKey();
         entry.value = mapEntry.getValue();
         adaptedMap.attribute.add(entry);
      }
      return adaptedMap;
   }

}