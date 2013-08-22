package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmt.addons.rti.throughput.Throughput;
import org.tmt.addons.rti.throughput.ThroughputDataReader;
import org.tmt.addons.rti.throughput.ThroughputSeq;
import org.tmt.addons.rti.throughput.ThroughputTypeSupport;

import com.persistent.bcsuite.base.SubscriberBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.SubscriberQos;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TopicQos;
import com.rti.ndds.transport.UDPv4Transport;

public class ThroughputSubscriber extends SubscriberBase {
   private static Logger logger = Logger.getLogger(ThroughputSubscriber.class);
   final static int MESSAGE_SIZE_MAX = 8192; // 65535
   final static int UDP_SIZE_MAX = 65536;
   final static int MAX_SAMPLES = 100;

   /** RTI specific declarations start **/
   // Common across all threads of subscriber
   private static DomainParticipantFactory factory = null;

   // Participant is per thread
   private DomainParticipant participant = null;
   DomainParticipantFactoryQos factory_qos = null;
   DomainParticipantQos participant_qos = null;
   SubscriberQos subscriber_qos = null;
   Subscriber subscriber = null;

   // And a data reader to do the work
   DataReaderQos data_reader_qos = null;
   ThroughputDataReader data_reader = null;
   ThroughputListener data_listener = null;

   // And a writer to communicate commands
   DataReaderQos command_reader_qos = null;
   TopicQos data_topic_qos = null;
   Topic data_topic = null;

   /** RTI specific declarations end **/

   /** Subscriber Program specific declarations **/

   private static int counter; // Keeps track of how many subscriber threads.
   private boolean isReliable = false;
   private String topicName = null;
   private int maxObjectsPerThread = 1024;

   @Override
   public void init(Map<String, String> attributes) {
      logger.info("Init called");
      String strReliable = attributes.get("isReliable");
      if (strReliable != null && !strReliable.isEmpty()) {
         isReliable = Boolean.parseBoolean(strReliable);
         logger.info("Setting Reliability " + isReliable);
      }

      String mo = attributes.get("maxObjectsPerThread");

      try {
         maxObjectsPerThread = Integer.parseInt(mo);
      } catch (Exception e) {
         logger.info("Cannot parse max-objects-per-thread.. using default as 1024");
         maxObjectsPerThread = 1024;
      }

      logger.info("Using maxObjectsPerThread as [" + maxObjectsPerThread + "]");

      topicName = attributes.get("topic");
      logger.info("Using topic as [" + topicName + "]");

      logger.info("In Read.. configuring all RTI objects");
      factory_qos = new DomainParticipantFactoryQos();

      participant_qos = new DomainParticipantQos();
      subscriber_qos = new SubscriberQos();

      // And a data writer to do the work
      data_reader_qos = new DataReaderQos();
      data_topic_qos = new TopicQos();

      // And a writer to communicate commands
      command_reader_qos = new DataReaderQos();

      /******************************************************************
       * UDP4 support
       *****************************************************************/
      UDPv4Transport.Property_t udpv4TransportProperty = new UDPv4Transport.Property_t();

      /******************************************************************
       * Other data
       *****************************************************************/

      String locator = null;

      if (factory == null) { // Once initialized no need to do it again for other threads.
         factory = DomainParticipantFactory.get_instance();
         RTIQosHelper.configure_factory_qos(factory_qos, factory, maxObjectsPerThread);
      }

      if (participant == null) {
         /** Participant Setting Starts **/
         RTIQosHelper.configure_participant_qos(participant_qos, factory, counter + 1);
         // Now we can create the 'disabled' participant.
         participant = factory.create_participant(88, participant_qos, null, StatusKind.STATUS_MASK_NONE); // 88 some
                                                                                                           // arbitrary
                                                                                                           // //
                                                                                                           // here.
         RTIQosHelper.configure_participant_transport(locator, udpv4TransportProperty, participant);
         // Now enable the participant
         participant.enable();
         /** Participant Setting Ends **/
      }
      else
      {
         logger.info("Reusing participant");
      }
      /** Subscriber Settings start **/
      participant.get_default_subscriber_qos(subscriber_qos);
      subscriber = participant.create_subscriber(subscriber_qos, null, StatusKind.STATUS_MASK_NONE); // create the
                                                                                                     // subscriber with
                                                                                                     // default QoS and
                                                                                                     // a default
                                                                                                     // listener
      /** Subscriber Settings end **/

      /******************************************************************
       * Set up Throughput topic and reader.
       *****************************************************************/
      // Now we register the data topic type with the participant.
      ThroughputTypeSupport.register_type(participant, ThroughputTypeSupport.get_type_name());
      // Get the default QoS for the Topic
      participant.get_default_topic_qos(data_topic_qos);

      // And create a Topic with QoS and default listener.
      // Note: Ownership is Shared by default.
      try {
         data_topic = participant.create_topic(topicName, ThroughputTypeSupport.get_type_name(), data_topic_qos, null,
                  StatusKind.STATUS_MASK_NONE);
      } catch (RETCODE_ERROR error) {
         data_topic = participant.find_topic(topicName, new Duration_t(3, 0));
         if (data_topic != null) {
            logger.info("[" + topicName + "] found, will use it now");
         } else
            throw error;
      }
      data_listener = new ThroughputListener();

      // Get the default reader QoS and configure to our requirements
      RTIQosHelper.configure_data_reader_qos(data_reader_qos, subscriber, isReliable,counter);

      data_reader = (ThroughputDataReader) subscriber.create_datareader(data_topic, data_reader_qos, data_listener,
               StatusKind.STATUS_MASK_ALL);
      data_reader.enable();
      incrementCounter(); // Add one to counter to indicate one thread.
      logger.info("Init Complete");
   }

   @Override
   public void read() {
      logger.info("Read called");
   }

   @Override
   public void shutdown() {      
      logger.info("Shutdown for subscriber called..");
      logger.info("Messages Recieved [" + data_listener.get_packetsReceived() + "], Lost ["
               + data_listener.get_packetsLost() + "]");
      decrementCounter();

      if (counter == 0) {
         logger.info("Counter is zero, deleting participant");
         if (participant != null) {
            try {
               participant.delete_contained_entities();
               DomainParticipantFactory.get_instance().delete_participant(participant);
            } catch (Exception e) {
               e.printStackTrace();
            }
            participant = null;
         }
         /*logger.info("Counter is zero, clearing factory instance now...");
         DomainParticipantFactory.finalize_instance();
         factory = null;
         logger.info("cleared factory instance");*/
      } else {
         logger.info("Counter is [" + counter + "],  not clearing factory instance");
      }
      markTaskComplete();
   }

   private static synchronized void incrementCounter() {
      counter++;
   }

   private static synchronized void decrementCounter() {
      counter--;
   }

   private class ThroughputListener extends DataReaderAdapter {
      ThroughputSeq dataSeq = new ThroughputSeq();
      SampleInfoSeq infoSeq = new SampleInfoSeq();

      private int _packetsReceived;
      private int _packetsLost;
      private long _sequenceNumber;

      public void on_data_available(DataReader reader) {
         ThroughputDataReader dataReader = (ThroughputDataReader) reader;

         try {
            dataReader.take(dataSeq, infoSeq, ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                     SampleStateKind.NOT_READ_SAMPLE_STATE, ViewStateKind.ANY_VIEW_STATE,
                     InstanceStateKind.ALIVE_INSTANCE_STATE);

            for (int i = 0; i < dataSeq.size(); ++i) {
               if (((SampleInfo) infoSeq.get(i)).valid_data) {
                  if (((Throughput) dataSeq.get(i)).sequence_number != _sequenceNumber) {
                     if (_sequenceNumber != 0) {
                        _packetsLost += (((Throughput) dataSeq.get(i)).sequence_number - _sequenceNumber);
                     }
                     _sequenceNumber = ((Throughput) dataSeq.get(i)).sequence_number;
                     // Reset sequence number
                  }
                  addToStatisticsPool(null, ((Throughput) dataSeq.get(i)).data.size(), new Date());
                  _packetsReceived++;
                  _sequenceNumber++; // Increment expected sequence
                  // number
               }
            }
            dataReader.return_loan(dataSeq, infoSeq);
         } catch (RETCODE_NO_DATA noData) {
            // No data to process
         }
      }

      public int get_packetsReceived() {
         return _packetsReceived;
      }

      public int get_packetsLost() {
         return _packetsLost;
      }

   }

}
