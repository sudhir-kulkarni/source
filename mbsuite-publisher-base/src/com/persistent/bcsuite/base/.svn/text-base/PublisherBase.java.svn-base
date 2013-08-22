package com.persistent.bcsuite.base;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.support.PublisherStatistics;

/**
 * This class should be extended to write the messaging platform specific Publisher program. <br>
 * 
 * <b>NOTE:</b> The suite will use a individual thread for one instance of this class. The suite will create "num_publishers'
 * instances of the child class, each with its own thread of execution. num_publishers is a property in the
 * configuration file.
 * <br>
 * The following methods should be overriden by the child class <br>
 * <br>
 * <li><pre>init(Map<String, String> attributes)</pre> 
 * <li><pre>sendMessageForThroughput(byte[] message)</pre> 
 * <li><pre>sendMessageForLatency(byte[] message)</pre> 
 * <li><pre>cleanup()</pre>
 * 
 */
public abstract class PublisherBase implements Runnable {
   private static final Logger logger = Logger.getLogger(PublisherBase.class.getName());
   private PublisherStatistics publisherStatistics;
   private byte[] message;
   protected int taskStatus = 0;
   private int thinkTimeInMillis = 0;
   private String messagePrefix = null;
   private boolean throughputOnly = false;
   private int messageLength;
   protected boolean canContinue = true;

   /**
    * This method extracts the thinkTime attribute from the configuration attributes and makes them available to all the
    * child classes. Override this method in case you want to access the attributes for your purpose. All initialization
    * activities like connecting to the messaging platform, initializing the classes should be done in the overridden
    * method of the child class.
    * 
    * @param attributes - The suite will fetch the attributes from the configuration file and pass it on to the 
    * child class
    */
   public void init(Map<String, String> attributes) {
      String ts = attributes.remove("thinkTime");
      if (ts != null) {
         try {
            thinkTimeInMillis = Integer.parseInt(ts);
         } catch (Exception e) {
         }
      }
      if (thinkTimeInMillis > 0)
         logger.info("Using thinkTime as " + thinkTimeInMillis + " ms");
   }

   public final void getReadyForNextIteration()
   {
      canContinue=false;      
   }
   
   public final void prepareForIteration()
   {
      canContinue=true;
      taskStatus=0;
   }
   /**
    * Implement this method in the messaging provider specific provider class. This method will be invoked by the suite
    * before it commands all publishers to run. This should be used for things like checking if the
    * subscribers have joined, if all required objects have been correctly initialized etc.
    * 
    * The suite will call this method on all tasks and only when all publishers return true will it proceed with calling the send* methods.
    * 
    * @return boolean, return true if your class is ready to start sending messages else false.
    */
   public abstract boolean isReadyToRun();
   /**
    * Override this method to write send logic and which captures throughput data. This method is invoked by the suite
    * when the configuration property dumpDetails=false.
    * 
    * 
    * @param message
    *           - The message as a byte array which needs to be sent
    */
   public abstract void sendMessageForThroughput(byte[] message);

   /**
    * Override this method to write send logic and which captures latency data. This method is invoked by the suite when
    * the configuration property dumpDetails=true.
    * 
    * @param message
    *           - The message as a byte array which needs to be sent
    */
   public abstract void sendMessageForLatency(byte[] message);

   /**
    * Implement this method in case there is any warmup processing needed.
    * 
    * @deprecated
    * @param message
    *           - The message as a byte array which needs to be sent
    */
   public void doWarmup(byte[] message) {
      logger.warn("No warmup process specified for the publisher class");
   }

   /**
    * Child class should implement this method.
    * <br>All cleanup activities needs to be done in this method. Any open handles connections, files should be closed,
    * cleaned in this method.
    */
   public abstract void cleanup();

   /**
    * This method is invoked by the suite when it wants to instruct all publisher programs to shutdown.
    */
   public final void shutdown() {
      canContinue = false;
      logger.info("Shutdown Interrupt Recieved by Publisher. Stopping sendMessage");
   }

   /**
    * This method should be used to log the statistics to the memory. sendMessageForThroughput and sendMessageForLatency
    * methods should use this method to provide the required information. The data collected in this method will be made
    * available to the reporting engine.
    * 
    * @param msgId
    *           - Unique Id of the message. Unique Id is needed only for the latency tests. each message consists a
    *           message_id appended by the actual message separated with a colon(:). Example a messageid can be
    *           P1T1:xxxxxxxxxxxxx or P2T2:xxxxxxxxxxx, the string before the colon is the message id.
    * @param messageSentDate
    *           - This is the message sent date with time component.
    * @param messageType
    *           - This is the message type to identify the type of message. Use 1 for text message, 2 for binary message
    *           etc.
    * @param messageSizeInBytes
    *           - This is the size of message in bytes.
    */
   protected void addToStatisticsPool(String msgId, Date messageSentDate, int messageType, int messageSizeInBytes) {
      publisherStatistics.addData(msgId, messageSentDate, messageType, messageSizeInBytes);
   }

   /**
    * Use this method in case you only need to capture the number of messages and the total bytes sent by the
    * publishers. This method wont capture the individual message details like messageId etc.
    * 
    * @param messageSizeInBytes
    */
   protected void addToStatisticsPool(int messageSizeInBytes) {
      publisherStatistics.addData(messageSizeInBytes);
   }

   
   /**
    *Use this method to add latency numbers for messages.
    * 
    * @param messageId - id of the message, can be a running number
    * @param latencyInNanos - latency in nano seconds.
    */
   protected void addLatencyStatistics(String messageId, long latencyInNanos) {
      publisherStatistics.addLatency(messageId, latencyInNanos);
   }
   
   
   /**
    * This method is invoked by the suite to inform the publisher program to prepare itself for run. This should not be
    * overriden by the child class.
    * 
    */
   public final void prepareToRun(byte[] message, boolean dumpDetails, String randomMessagePrefix, int messageLength) {
      this.messageLength = messageLength;
      throughputOnly = !dumpDetails;

      messagePrefix = randomMessagePrefix;

      this.message = message;
      logger.info("Publisher Ready to Run. Using Message Prefix [" + messagePrefix + "]");
   }

   @Override   
   public final void run() {
      long stTime = System.currentTimeMillis();

      if (throughputOnly)
      {
         publisherStatistics = new PublisherStatistics();
         sendMessageForThroughput(message);
      }
      else
      {
         publisherStatistics = new PublisherStatistics(200000);
         sendMessageForLatency(message);
      }
      taskStatus = 1;
      long edTime = System.currentTimeMillis();
      logger.debug("Actual time take by this publisher thread = " + (edTime - stTime) + " ms");
      logger.info("Thread finished sending messages");
   }

   /**
    * This method provides a handle to the publisher statistics collected during
    * the publisher program execution.
    * @return
    */
   public final PublisherStatistics getPublisherStatistics() {
      return publisherStatistics;
   }

   /**
    * This method will return true if the publisher program has completed execution. The publisher program has to invoke
    * the markTaskComplete method to allow the suite to know that it has completed execution.
    * 
    * @return boolean - returns true if the publisher has completed its activity. The publisher program has to invoke the <pre>markTaskComplete()</pre> to mark itself as completed.
    */
   public boolean isTaskComplete() {
      if (taskStatus == 1)
         return true;

      return false;
   }

   /**
    * When the publisher program finishes its work, invoke this method to inform the suite that the publisher program
    * has completed. If this method is not invoked the suite will think that the publisher is still executing.
    */
   protected void markTaskComplete() {
      taskStatus = 1;
   }

   /**
    * This method returns the length of the message provided by the suite to the publisher program.
    * 
    * @return
    */
   protected int getMessageLength() {
      return this.messageLength;
   }

   /**
    * This method identifies this instance uniquely. To generate a unique message number you should append the
    * messagecounter to this string and add it to the statistics pool.
    * 
    * @return
    */
   protected String getMessagePrefix() {
      return this.messagePrefix;
   }

   /**
    * Invoke this method to inform the suite that the publisher program has started sending messages.
    * 
    * @param messageSendStartDate
    *           - date/time when the send started
    */
   protected void setStartTime(Date messageSendStartDate) {
      publisherStatistics.setProcessStartTime(messageSendStartDate);
   }

   /**
    * Invoke this method to inform the suite that the publisher program has stopped sending messages.
    * 
    * @param messageSendCompletedDate
    *           - date/time when the send stopped
    */
   protected void setEndTime(Date messageSendCompletedDate) {
      publisherStatistics.setProcessEndTime(messageSendCompletedDate);
   }

   /**
    * This method returns the think time in milliseconds as configured in the configuration file. This method will
    * return 0, if no think time was configured.
    * 
    * @return
    */
   protected int getThinkTimeInMillis() {
      return this.thinkTimeInMillis;
   }

   /**
    * This method will return false if the suite wants the publisher to stop sending messages If your publisher program
    * runs in a loop, use this method as its exit point.
    * 
    * @return
    */
   protected boolean canContinue() {
      return canContinue;
   }
}
