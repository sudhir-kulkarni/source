package com.persistent.bcsuite.base;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.support.SubscriberStatistics;

/**
 * This class should be extended to write the messaging platform specific Subscriber program. <br>
 * 
 * <b>NOTE:</b> The suite will use a individual thread for one instance of this class. The suite will create "num_subscribers'
 * instances of the child class, each with its own thread of execution. num_subscribers is a property in the
 * configuration file.
 * <br>
 * The following methods should be overriden by the child class <br>
 * <br>
 * <li><pre>init(Map<String, String> attributes)</pre> 
 * <li><pre>read()</pre> 
 * <li><pre>shutdown()</pre> 
 *
 */
public abstract class SubscriberBase implements Runnable {
   private static final Logger logger = Logger.getLogger(SubscriberBase.class);
   private SubscriberStatistics subscriberStatistics;
   private int taskStatus = 0;
   boolean dumpDetail = false;

   /**
    * Implement this method access the attributes for your Subscriber program. All initialization
    * activities like connecting to the messaging platform, initializing the classes should be done in this
    * overridden method of the child class.
    * 
    * @param attributes - The suite will fetch the attributes from the configuration file and pass it on to the 
    * child class via this method.
    */
   public abstract void init(Map<String, String> attributes);

   /**
    * Implement this method to write the message read logic specific to the message platform.
    * This will be invoked by the suite to start the process of reading the messages from the source queue or topic 
    */
   public abstract void read();

   /**
    * Its important to implement this method. The suite will invoke this method and expect that 
    * the subscriber program does all the shutdown activities like closing connections, cleaning up resources
    * releasing any resource handles etc.
    */
   public abstract void shutdown();

   /**
    * Final method, cannot be overridden in the Subscriber program.
    * @param dumpDetails
    */
   public final void prepareToRun(boolean dumpDetails) {
      this.dumpDetail = dumpDetails;
      if (dumpDetails)
         subscriberStatistics = new SubscriberStatistics(200000);
      else
         subscriberStatistics = new SubscriberStatistics();
   }

   @Override
   public void run() {
      try {
         if (dumpDetail){
            subscriberStatistics = new SubscriberStatistics(200000);}
         else{
            subscriberStatistics = new SubscriberStatistics();}
         
         
         this.read();
      } catch (Throwable t) {
         logger.debug("Exception from Subscriber read() method. Marking Subscriber as complete.");
         logger.debug(t);
      }
      markTaskComplete();
   }

   /**
    * Returns the counters and the statistics captured by this particular Subscriber instance.
    * @return
    */
   public SubscriberStatistics getSubscriberStatistics() {
      return subscriberStatistics;
   }

   /**
    * returns true if the taskStatus is set to 1. Use the markTaskComplete() method to
    * mark the task complete.
    * @return boolean - status of the task.
    */
   public boolean isTaskComplete() {
      if (taskStatus == 1)
         return true;

      return false;
   }

   /**
    * Use this method to mark the subscriber program complete. In a typical situation
    * this method should be invoked at the end of the read method.
    */
   public void markTaskComplete() {
      taskStatus = 1;
   }

   /**
    * Store the various counters collected by using this method.
    * 
    * @param msgId - msgId
    *           - Unique Id of the message. Unique Id is needed only for the latency tests. each message consists a
    *           message_id appended by the actual message separated with a colon(:). Example a messageid can be
    *           P1T1:xxxxxxxxxxxxx or P2T2:xxxxxxxxxxx, the string before the colon is the message id.
    * @param messageSize
    *          - This is the size of message in bytes.
    * @param recdOn
    *          -Date/Time when this message was read.
    */
   public void addToStatisticsPool(String msgId, int messageSize, Date recdOn,long latency) {
      subscriberStatistics.addData(msgId, recdOn, messageSize,latency);
   }
   
   
   public void addToStatisticsPool(String msgId, int messageSize, Date recdOn) {
      subscriberStatistics.addData(msgId, recdOn, messageSize,0);
   }
}
