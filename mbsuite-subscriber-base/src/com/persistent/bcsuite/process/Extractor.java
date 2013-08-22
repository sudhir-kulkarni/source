package com.persistent.bcsuite.process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.SubscriberBase;
import com.persistent.bcsuite.beans.SubscriberGroup;
import com.persistent.bcsuite.beans.SubscriberSettings;
import com.persistent.bcsuite.support.SubscriberDetail;
import com.persistent.bcsuite.support.Validator;

public class Extractor {
   private final static Logger logger = Logger.getLogger(Extractor.class);

   // Initialize once per every run of process

   SubscriberGroup sg = null;
   List<SubscriberBase> tasksList = new ArrayList<SubscriberBase>();
   double totalMessagesRecd = 0d;
   double elapsedTimeInSecs = 1d;
   double totalTimeTakenToReadAllMessages = 0d;
   double messagesPerSecond = 0d;
   double totalBytesRead = 0d;
   double totalBytesinMB = 0d;
   double megaBytesReadPerSecond = 0d;
   Date subscriberReadingStartTime = new Date(Long.MAX_VALUE);
   Date subscriberReadEndTime = new Date(0);
   String[] topicArr;

   // Initialize once per instance
   String instanceToken;
   String taskKey;
   String groupKey;
   SubscriberSettings subscriberSettings;
   private boolean possibleHangingSubscribers = false;
   private int runCounter = 0;
   private String dbUrl=null;
   private static String dumpDestination; 

   private void generateInstanceToken() {
      if (instanceToken == null)
         instanceToken = UUID.randomUUID().toString();

      logger.info("Using Token [" + instanceToken + "]");
   }

   public boolean isPossibleHangingSubscribers() {
      return possibleHangingSubscribers;
   }

   public Extractor(String taskKey, String groupKey, String token) {
      this.taskKey = taskKey;
      this.groupKey = groupKey;
      this.instanceToken = token;
   }

   public static void main(String args[]) {

      // Validate arguments
      if (!Validator.areArgumentsValid(args))
         return;

      int repeatCounter = Validator.getRepeatCounter(args);

      Extractor extractor = new Extractor(args[0], args[1], args[2]);
      extractor.init();

      // Loop for number of repetitions requested. If no repetitions requests
      // will run once.
      logger.info("################################ Test Process started ################################ ");
      for (int i = 0; i < repeatCounter; i++) {
         System.gc();
         if (extractor.isPossibleHangingSubscribers()) {
            logger.warn("In the earlier run some subscribers did not finish and are possibly hanging. Cannot continue with a new run");
            break;
         }
         extractor.process();
      }
      logger.info("################################ Test Process finished ################################ ");
      System.exit(0);
   }

   private void init() {
      try {
         InputStream is = this.getClass().getClassLoader().getResourceAsStream("subscriber-config.xml");
         JAXBContext jaxbContext = JAXBContext.newInstance(SubscriberSettings.class);

         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
         subscriberSettings = (SubscriberSettings) jaxbUnmarshaller.unmarshal(is);
         logger.info("Sucessfully loaded subscriber configuration");
         generateInstanceToken();
		 
		 InputStream commonSettingsInputStream = this.getClass().getClassLoader().getResourceAsStream("common-settings.properties");
		 Properties commonSettings = new Properties();
		 commonSettings.load(commonSettingsInputStream);
		 dbUrl = commonSettings.getProperty("db-url");
		 dumpDestination = commonSettings.getProperty("dump-destination");
		 logger.info("Sucessfully Loaded common-settings properties");
		 
      } catch (JAXBException e) {
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void process() {
      Connection conn = null;
      String insertSQL = "insert into subscriber_detail(token,iteration,latency_in_ns) values (?,?,?)";
		String DETAILS_COLUMN = "token,iteration,latency_in_ns";
      resetValues();
      logger.info("\nTest Run [" + ++runCounter + "] started >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      List<SubscriberGroup> subscriberGroups = subscriberSettings.getSubscriberGroups();

      if (subscriberGroups == null || subscriberGroups.size() == 0) {
         logger.error("No subscriber groups in the configuration. Cannot proceed");
         System.exit(0);
      }

      // Identify the correct subscriber group based on the key provided in input.
      for (SubscriberGroup subscriberGroup : subscriberGroups) {
         if (subscriberGroup.getName().equals(groupKey)) {
            sg = subscriberGroup;
            break;
         }
      }

      if (sg == null) {
         logger.error("No subscriber group found with name [" + groupKey + "].Cannot proceed");
         System.exit(0);
      }

      topicArr = createTopicList(sg.getNumTopics(), taskKey);

      // Do equal distribution of subscribers on topics.
      if (sg.getSubscriberDistribution() == null
               || SubscriberGroup.EQUAL_DISTRIBUTION.equals(sg.getSubscriberDistribution()))
         processForEqualDistribution();

      logger.info("Waiting till specified shutdown delay of [" + sg.getSubscriberShutdownDelay() + "] secs.");

      try {
         Thread.sleep(sg.getSubscriberShutdownDelay() * 1000);
         System.out.println("Crossed Shutdown Delay. Sending shutdown interrupt to all subscribers.");
      } catch (InterruptedException e) {
         logger.error("Interrupted Shutdown Delay Sleep");
      }

      for (SubscriberBase task : tasksList) {
         task.shutdown();
      }

      boolean allTasksFinished = false;
      PreparedStatement psDetail = null;
      try {
         Class.forName("com.mysql.jdbc.Driver").newInstance();
         conn = DriverManager.getConnection(dbUrl);
         conn.setAutoCommit(true);
         psDetail = conn.prepareStatement(insertSQL);
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InstantiationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      while (!allTasksFinished) {
         allTasksFinished = true;
         for (SubscriberBase task : tasksList) {
            if (!task.isTaskComplete())
               allTasksFinished = false;
         }

         if (!allTasksFinished) {
            try {
               logger.info("Some subscribers not finished yet. Have to wait for their clean shutdown.");
               Thread.sleep(2000);
            } catch (InterruptedException e1) {
               logger.error("Not all subscribers have finished their work. Sleep interrupted. Possible hannging threads");
               possibleHangingSubscribers = true;
            }
         }
      }
      if (allTasksFinished)
         logger.info("All subscribers have finished sucessfully.");

      int batchCount = 0;
      totalBytesRead = 0;
      totalMessagesRecd = 0;
      for (SubscriberBase task : tasksList) {

         if (task.getSubscriberStatistics() != null) {
            if (task.getSubscriberStatistics().getProcessStartTime().getTime() < subscriberReadingStartTime.getTime())
               subscriberReadingStartTime = task.getSubscriberStatistics().getProcessStartTime();

            if (task.getSubscriberStatistics().getProcessEndTime().getTime() > subscriberReadEndTime.getTime())
               subscriberReadEndTime = task.getSubscriberStatistics().getProcessEndTime();

            totalBytesRead += task.getSubscriberStatistics().getTotalBytesRecd();
            totalMessagesRecd += task.getSubscriberStatistics().getTotalMessagesRecd();

            List<SubscriberDetail> statisticsList = task.getSubscriberStatistics().getData();
		}

      elapsedTimeInSecs = (double) (subscriberReadEndTime.getTime() - subscriberReadingStartTime.getTime()) / 1000;
      totalBytesinMB = (double) totalBytesRead / (1024 * 1024);
      messagesPerSecond = (double) totalMessagesRecd / elapsedTimeInSecs;
      megaBytesReadPerSecond = (double) totalBytesinMB / elapsedTimeInSecs;
      }
      // addStatisticsToResult(new HashMap<String, String>());
      try {
			if("db".equalsIgnoreCase(dumpDestination) || dumpDestination==null || dumpDestination.trim().length()==0)
			{
			addSummaryToDB(new HashMap<String, String>());
			}
			else
			{
			addSummaryToFile(new HashMap<String, String>());
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      logger.info("Test Run [" + runCounter + "] finished <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
      try {
         if (conn != null) {
            conn.close();
         }

      } catch (SQLException e) {
      }      
   }

   private String[] createTopicList(int numTopics, String key) {
      String[] topics = new String[numTopics];

      for (int i = 0; i < numTopics; i++) {
         topics[i] = key + "-topic" + i;
      }

      return topics;
   }

   private void resetValues() {
      sg = null;
      if (tasksList != null)
         tasksList.clear();

      tasksList = new ArrayList<SubscriberBase>();
      totalMessagesRecd = 0d;
      elapsedTimeInSecs = 1d;
      totalTimeTakenToReadAllMessages = 0d;
      messagesPerSecond = 0d;
      totalBytesRead = 0d;
      totalBytesinMB = 0d;
      megaBytesReadPerSecond = 0d;
      subscriberReadingStartTime = new Date(Long.MAX_VALUE);
      subscriberReadEndTime = new Date(0);
   }

   private SubscriberBase getTask(String className) {
      try {
         SubscriberBase t = (SubscriberBase) Class.forName(className).newInstance();
         return t;
      } catch (InstantiationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   private void processForEqualDistribution() {
      int totalSubscribersPerTopic = sg.getTotalSubscribers() / topicArr.length;
      if (totalSubscribersPerTopic < 1) {
         logger.error("You havent specified enough subscribers for the topics. Specify num_subscribers such that they can be evenly distributed to the topics");
         throw new RuntimeException("Not enough subscribers specified in config");
      }
      int totalTopics = topicArr.length;
      logger.info("Total [" + totalTopics + "] to be used");
      if (totalTopics == 0) {
         logger.error("No topics specified in the config. Cannot proceed");
         System.exit(0);
      }
      String clazzName = subscriberSettings.getTasks().get(taskKey);
      for (int topics = 0; topics < totalTopics; topics++) {
         String tp = topicArr[topics];
         logger.info("Configuring subscriber for topic [" + tp + "]");
         HashMap<String, String> attrMapToSubscriber = null;
         if (sg.getAttributes() == null) {
            attrMapToSubscriber = new HashMap<String, String>(1);
         } else {
            attrMapToSubscriber = new HashMap<String, String>(sg.getAttributes());
         }

         attrMapToSubscriber.put("topic", tp);
         logger.info("Attribute topic added to map");
         for (int st = 0; st < totalSubscribersPerTopic; st++) {
            SubscriberBase sb = getTask(clazzName);
            logger.debug("Invoking prepareToRun");
            sb.prepareToRun(sg.isDumpDetails());
            logger.debug("Invoking init");
            sb.init(attrMapToSubscriber);
            logger.debug("Adding to list");
            tasksList.add(sb); // Dont start.
         }
      }

      if (sg.getWarmUpTimeInMillis() > 0) {
         logger.info("Warming up as specified in the config for " + sg.getWarmUpTimeInMillis() + " ms");
         try {
            Thread.sleep(sg.getWarmUpTimeInMillis());
         } catch (InterruptedException ie) {
            logger.error("Warmup Interrupted...");
         }
         logger.info("Warm up over !!");
      }

      // Start all subscribers at the same time.
      for (SubscriberBase subscriberInstance : tasksList) {
         Thread t = new Thread(subscriberInstance);
         t.start();
      }

   }

	private void addSummaryToDB(Map<String, String> attributes)
			throws Exception {
		Connection conn = null;
		String insertSQL = "insert into subscriber_summary(token,iteration,start_date,end_date,num_subscribers,total_messages_recd,tput_msg_per_sec,total_mb_recd,tput_mb_per_sec,attributes,test_group_name,task_name) values (?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(dbUrl);
			conn.setAutoCommit(true);

			if (conn != null) {
				StringBuffer sbf = new StringBuffer();
				if (attributes != null && attributes.size() > 0) {
					for (Entry entry : attributes.entrySet()) {
						sbf.append(entry.getValue()).append(",");
					}
				}
				logger.info("Adding data to DB with token [" + instanceToken
						+ "]");
				PreparedStatement ps = conn.prepareStatement(insertSQL);
				ps.setString(1, instanceToken);
				ps.setInt(2, runCounter);
				ps.setTimestamp(3, new java.sql.Timestamp(
						subscriberReadingStartTime.getTime()));
				ps.setTimestamp(4,
						new java.sql.Timestamp(subscriberReadEndTime.getTime()));
				ps.setInt(5, sg.getTotalSubscribers());
				ps.setInt(6, new Double(totalMessagesRecd).intValue());
				ps.setInt(7, new Double(messagesPerSecond).intValue());
				ps.setInt(8, new Double(totalBytesinMB).intValue());
				ps.setDouble(9, megaBytesReadPerSecond);
				ps.setString(10, sbf.toString());
				ps.setString(11, groupKey);
				ps.setString(12, taskKey);

				ps.execute();
			}
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}

	}

	private void addSummaryToFile(Map<String, String> attributes)
			 {

		String SUMMARY_COLUMN = "token,iteration,start_date,end_date,num_subscribers,total_messages_recd,tput_msg_per_sec,total_mb_recd,tput_mb_per_sec,attributes,test_group_name,task_name";

		String[] spliit_on_comma_for_summary = SUMMARY_COLUMN.split(",");
		FileWriter fileWriter = null;
		File dir = null;
		File file = null;
		
		URL location =  this.getClass().getResource("/csv");
	    String exportPath = location.getPath();
	    
		dir = new File(exportPath);
		if (!dir.exists()) {
			// if dir is not exixt then create it
			dir.mkdir();
		}
	
		try {
		
			file=new File(exportPath+File.separator+"SubscriberSummary.csv");

			fileWriter = new FileWriter(file, true);

			if (file.length() == 0) {

				for (int i = 0; i < spliit_on_comma_for_summary.length; i++) {
					String[] c = spliit_on_comma_for_summary[i].split(":");
					fileWriter.append(c[0]);
					if (i != spliit_on_comma_for_summary.length - 1) {
						fileWriter.append(",");
					}
				}
				fileWriter.append("\n");

			}

			try {

				StringBuffer sbf = new StringBuffer();
				if (attributes != null && attributes.size() > 0) {
					for (Entry entry : attributes.entrySet()) {
						sbf.append(entry.getValue()).append(":");
					}
					logger.info("Adding data to file with token ["
							+ instanceToken + "]");
				}
					fileWriter.append(instanceToken);
					fileWriter.append(",");
					fileWriter.append(String.valueOf(runCounter));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(new java.sql.Timestamp(
							subscriberReadingStartTime.getTime())));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(new java.sql.Timestamp(
							subscriberReadEndTime.getTime())));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(sg.getTotalSubscribers()));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(new Double(
							totalMessagesRecd).intValue()));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(new Double(
							messagesPerSecond).intValue()));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(new Double(totalBytesinMB)
							.intValue()));
					fileWriter.append(",");
					fileWriter.append(String.valueOf(megaBytesReadPerSecond));
					fileWriter.append(",");
					fileWriter.append(sbf.toString());
					fileWriter.append(",");
					fileWriter.append(groupKey);
					fileWriter.append(",");
					fileWriter.append(taskKey);
					fileWriter.append("\n");

				
			} finally {

				fileWriter.close();

			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
