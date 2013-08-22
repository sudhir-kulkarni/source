package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmt.addons.rti.throughput.Throughput;
import org.tmt.addons.rti.throughput.ThroughputDataWriter;
import org.tmt.addons.rti.throughput.ThroughputTypeSupport;

import com.persistent.bcsuite.base.PublisherBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_TIMEOUT;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.FlowController;
import com.rti.dds.publication.FlowControllerProperty_t;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.Publisher;
import com.rti.dds.publication.PublisherQos;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TopicQos;
import com.rti.ndds.transport.UDPv4Transport;

public class ThroughputPublisher extends PublisherBase {
   public static Logger logger = Logger.getLogger(ThroughputPublisher.class);

   /** RTI specific declarations start **/
   private static DomainParticipantFactory factory = null;
   private static DomainParticipant participant = null;

   DomainParticipantFactoryQos factory_qos = null;
   DomainParticipantQos participant_qos = null;
   PublisherQos publisher_qos = null;
   Publisher publisher = null;

   // And a data writer to do the work
   DataWriterQos data_writer_qos = null;
   ThroughputDataWriter data_writer = null;
   InstanceHandle_t data_instanceHandle = null;

   boolean large_data = false;
   FlowController flow_controller = null;
   FlowControllerProperty_t flow_controller_property = new FlowControllerProperty_t();

   TopicQos data_topic_qos = null;
   Topic data_topic = null;
   Throughput data_instance = null;
   /** RTI specific declarations end **/

   
   /** Publisher program specific declarations start **/
   private static int counter;
   private int throttlingFactor = 100;
   boolean isReliable = false;
   boolean isAsynch = false;
   String topicName = null;
   private int maxObjectsPerThread = 1024;
   private int numSubscribers=0;
   /** Publisher program specific declarations start **/
   
   @Override
   public boolean isReadyToRun() {
      boolean isReadyToRun = false;
      PublicationMatchedStatus pubMatch = new PublicationMatchedStatus();
      data_writer.get_publication_matched_status(pubMatch);
      if (pubMatch.current_count == numSubscribers) {
         logger.info("All subscribers matched");
         isReadyToRun = true;
      } else {
         logger.info("Not Matched ");

      }
      return isReadyToRun;
   }

   @Override
   public void sendMessageForThroughput(byte[] message) {
      int len = message.length;
      int exceptionCnt = 0;
      // Set up initial Data
      data_instance.sequence_number = 0;
      setStartTime(new Date());
      int thinkTime = getThinkTimeInMillis();
      data_instance.data.clear();
      data_instance.data.addAllByte(message);
      while (canContinue()) {
         try {
            for (int l = 0; l < throttlingFactor && canContinue; l++, data_instance.sequence_number++) {
               try {
                  data_writer.write(data_instance, data_instanceHandle);
                  addToStatisticsPool(len);
               } catch (RETCODE_TIMEOUT timeout) {
                  data_instance.sequence_number--;
                  exceptionCnt++;
               }
            }
            try {
               Thread.sleep(thinkTime);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         } catch (Exception ex) {
            logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = " + ex);
         }

      }
      setEndTime(new Date());
      logger.info("Sent [" + data_instance.sequence_number + "] , Exceptions [" + exceptionCnt + "]");
      logger.info("Sleeping for 5 seconds");
      try {
         Thread.sleep(5000);
      } catch (InterruptedException e1) {
         e1.printStackTrace();
      } finally {
         markTaskComplete();
      }
   }

   @Override
   public void sendMessageForLatency(byte[] message) {
      // TODO Auto-generated method stub

   }

   @Override
   public void init(Map<String, String> attributes) {
      super.init(attributes);
      String strReliable = attributes.get("isReliable");
      if (strReliable != null && !strReliable.isEmpty()) {
         isReliable = Boolean.parseBoolean(strReliable);
         logger.info("Setting Reliability " + isReliable);
      }

      String tf = attributes.get("throttlingFactor");

      try {
         throttlingFactor = Integer.parseInt(tf);
      } catch (Exception e) {
         logger.error("Cannot parse throttling factor.. using default as 100");
         throttlingFactor = 100;
      }
      
      String ns = attributes.get("numSubscribers");

      try {
         numSubscribers = Integer.parseInt(ns);
      } catch (Exception e) {
         logger.error("Cannot parse numSubscribers.. using default as 1");
         throttlingFactor = 1;
      }

      logger.info("Using numSubscribers[" + numSubscribers + "]");

      
      String mo = attributes.get("maxObjectsPerThread");

      try {
         maxObjectsPerThread = Integer.parseInt(mo);
      } catch (Exception e) {
         logger.error("Cannot parse max-objects-per-thread.. using default as 1024");
         maxObjectsPerThread = 1024;
      }

      logger.info("Using maxObjectsPerThread as [" + maxObjectsPerThread + "]");

      topicName = attributes.get("topic");

      logger.info("Using topic as [" + topicName + "]");

      // ##########################################
      factory_qos = new DomainParticipantFactoryQos();

      participant_qos = new DomainParticipantQos();
      publisher_qos = new PublisherQos();

      // And a data writer to do the work
      data_writer_qos = new DataWriterQos();
      
      data_instanceHandle = InstanceHandle_t.HANDLE_NIL;
      data_topic_qos = new TopicQos();

      /******************************************************************
       * Plug-in transport variables
       *****************************************************************/
      UDPv4Transport.Property_t udpv4TransportProperty = new UDPv4Transport.Property_t();

      /******************************************************************
       * Other data
       *****************************************************************/

      // Time in milliseconds
      String locator = null;
      boolean useMulticast = false;
      int max_gather_send_buffers = 16;


      if (factory == null) {
         factory = DomainParticipantFactory.get_instance();
         RTIQosHelper.configure_factory_qos(factory_qos, factory, maxObjectsPerThread);
      }

      if(participant == null)
      {
         RTIQosHelper.configure_participant_qos(participant_qos, factory, counter);
         // Now we can create the 'disabled' participant.
         participant = factory.create_participant(88, participant_qos, null, StatusKind.STATUS_MASK_NONE);
         RTIQosHelper.configure_participant_transport(locator, udpv4TransportProperty, participant);
         participant.enable();
      }
      else
      {
         logger.info("Reusing participant");
      }
      
      // Create the rest of the entities
      if (large_data || isAsynch) {
         /* Configure flow controller Quality of Service */
         configure_flow_controller_property(flow_controller_property, participant, large_data);
         /* Create flowcontroller */
         flow_controller = participant.create_flowcontroller("throughput test flow_controller",
                  flow_controller_property);
      }

      // Create the rest of the entities
      participant.get_default_publisher_qos(publisher_qos);
      // And create the publisher with default QoS and a default listener
      publisher_qos.asynchronous_publisher.thread.priority = Thread.MAX_PRIORITY - 1;

      publisher = participant.create_publisher(publisher_qos, null, StatusKind.STATUS_MASK_NONE);

      /******************************************************************
       * Set up Throughput topic and writer.
       *****************************************************************/
      // Now we register the data topic type with the participant.
      ThroughputTypeSupport.register_type(participant, ThroughputTypeSupport.get_type_name());

      // Get the default QoS for the Topic
      participant.get_default_topic_qos(data_topic_qos);

      // And create a Topic with Default QoS and default listener.
      // Note: Ownership is Shared by default.
      try{
         data_topic = participant.create_topic(topicName, ThroughputTypeSupport.get_type_name(), data_topic_qos, null,
               StatusKind.STATUS_MASK_NONE);
      }catch (RETCODE_ERROR error) {
         data_topic = participant.find_topic(topicName, new Duration_t(3,0));
         if(data_topic != null)
         {
            logger.info("["+topicName+"] found, will use it now");
         }
         else
            throw error;
      }
      /* set up user data */
      data_instance = new Throughput();

      int samples_per_trigger = RTIQosHelper.configure_data_writer_qos(data_writer_qos, large_data,
               max_gather_send_buffers, publisher, useMulticast, isAsynch, isReliable,counter);

      data_writer = (ThroughputDataWriter) publisher.create_datawriter(data_topic, data_writer_qos, null,
               StatusKind.STATUS_MASK_NONE);
      data_writer.enable();
      /*
       * Register Topic instance with data writer. Note: because we are not using keys this will return DDS_HANDLE_NIL
       */
      data_instanceHandle = data_writer.register_instance(data_instance);

      data_instance.data.clear();
      data_instance.data.setMaximum(getMessageLength());

      

      // ##############################################################
      incrementCounter();

      logger.info("Init Complete");
   }

   public void configure_flow_controller_property(FlowControllerProperty_t flow_controller_property,
            DomainParticipant participant, boolean large_data) {

      participant.get_default_flowcontroller_property(flow_controller_property);

      /*
       * Approach: send small bursts as fast as possible (as opposed to sending larger bursts, but slower)
       */
      if (large_data && RTIQosHelper.bw_limit > 0) {
         flow_controller_property.token_bucket.tokens_added_per_period = (int) ((float) RTIQosHelper.bw_limit * 1000000.0 / 8.0 / 100.0 / 1024.0);
      } else {
         flow_controller_property.token_bucket.tokens_added_per_period = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
      }
      flow_controller_property.token_bucket.max_tokens = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
      flow_controller_property.token_bucket.tokens_leaked_per_period = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
      // tenMilliSec
      flow_controller_property.token_bucket.period.sec = 0;
      flow_controller_property.token_bucket.period.nanosec = 10 * 1000000;
      flow_controller_property.token_bucket.bytes_per_token = 1024;
      /* minimum value */
   }

   private synchronized void incrementCounter() {
      counter++;
   }

   private synchronized void decrementCounter() {
      counter--;
   }

   @Override
   public void cleanup() {
      logger.info("Executing cleanup...");
      logger.info("Sent [" + data_instance.sequence_number +"] messages from this subscriber");
      decrementCounter();

      if(counter ==0)
      {
         logger.info("Counter is zero, deleting participant instance now...");
         if (participant != null) {
            participant.delete_contained_entities();
            DomainParticipantFactory.get_instance().delete_participant(participant);
            logger.info("Participant Deleted");
            participant = null;
         }         

            logger.info("Counter is zero, clearing factory instance now...");
            DomainParticipantFactory.finalize_instance();
            factory=null;
            logger.info("cleared factory instance");
         }
         else
         {
            logger.info("Counter is ["+counter+"],  not clearing factory instance");
         }
      }

   

}
