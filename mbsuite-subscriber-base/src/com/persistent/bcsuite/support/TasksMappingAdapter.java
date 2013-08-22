package com.persistent.bcsuite.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TasksMappingAdapter extends XmlAdapter<TasksMappingAdapter.MapperMap, Map<String, String>> {

   public static class MapperMap {
      public List<Task> task = new ArrayList<Task>();
   }

   public static class Task {
      public String key;
      public String clazz;
   }

   @Override
   public Map<String, String> unmarshal(MapperMap adaptedMap) throws Exception {
      Map<String, String> map = new HashMap<String, String>();
      for (Task task : adaptedMap.task) {
         map.put(task.key, task.clazz);
      }
      return map;
   }

   @Override
   public MapperMap marshal(Map<String, String> map) throws Exception {
      MapperMap adaptedMap = new MapperMap();
      for (Map.Entry<String, String> mapEntry : map.entrySet()) {
         Task task = new Task();
         task.key = mapEntry.getKey();
         task.clazz = mapEntry.getValue();
         adaptedMap.task.add(task);
      }
      return adaptedMap;
   }

}
