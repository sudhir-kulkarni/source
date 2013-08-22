package com.persistent.bcsuite.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * This class is responsible for distributing the load on topics based on the load distribution specified in the
 * configuration file. In case of a balanced load configuration the suite will attempt to provide equal number of
 * subscribers to each topic provided that the number of publishers are more than the number of topics.
 * 
 * 
 */
public class LoadDistributionManager {
   private static final Logger logger = Logger.getLogger(LoadDistributionManager.class);

   public static Map<String, Integer> generateLoadDistribution(String[] topicArray, int totalPublishers,
            String loadDistributionStrategy) {
      Map<String, Integer> loadDistributionMap = new HashMap<String, Integer>();
      int totalTopics = topicArray.length;

      if ("unbalanced".equalsIgnoreCase(loadDistributionStrategy)) {
         logger.info("Using [" + loadDistributionStrategy + "] strategy");

         for (String tp : topicArray) {
            loadDistributionMap.put(tp, 0);
         }

         Random r = new Random();

         for (int i = 0; i < totalPublishers; i++) {
            int index = r.nextInt(totalTopics);
            int lookupIndex = -1;

            if (index == 0)
               lookupIndex = index;
            else
               lookupIndex = index - 1;

            String topic = topicArray[lookupIndex];
            Integer pubs = loadDistributionMap.get(topic);
            loadDistributionMap.put(topic, pubs++);
         }
      } else {
         int publishersPerTopic = totalPublishers / totalTopics;
         for (String tp : topicArray) {
            loadDistributionMap.put(tp, publishersPerTopic);
         }
      }
      return loadDistributionMap;
   }
}
