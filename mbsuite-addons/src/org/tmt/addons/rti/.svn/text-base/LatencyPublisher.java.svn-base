package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmt.addons.rti.throughput.Throughput;
import org.tmt.addons.rti.throughput.ThroughputDataReader;
import org.tmt.addons.rti.throughput.ThroughputDataWriter;
import org.tmt.addons.rti.throughput.ThroughputSeq;
import org.tmt.addons.rti.throughput.ThroughputTypeSupport;

import com.persistent.bcsuite.base.PublisherBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.ByteSeq;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.HistoryQosPolicyKind;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.OwnershipQosPolicyKind;
import com.rti.dds.infrastructure.PublishModeQosPolicyKind;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.RETCODE_TIMEOUT;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.TransportBuiltinKind;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.FlowController;
import com.rti.dds.publication.FlowControllerProperty_t;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.Publisher;
import com.rti.dds.publication.PublisherQos;
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
import com.rti.ndds.transport.ShmemTransport;
import com.rti.ndds.transport.TransportSupport;
import com.rti.ndds.transport.UDPv4Transport;
import com.rti.ndds.transport.UDPv6Transport;

public class LatencyPublisher extends PublisherBase {

   public static Logger logger = Logger.getLogger(LatencyPublisher.class);
   private static int counter;
   final static int MESSAGE_SIZE_MAX = 8192; // 65535
   final static int DEFAULT_MAX_BLOCKING_TIME_NS = 999000000;
   final static int MAX_SAMPLES = 100;
   final static int NANOSEC_PER_MILLISEC = 1000000;
   final static int MAX_EVENT_COUNT = (1024 * 16);
   final static int UDP_SIZE_MAX = 65536;
   private Duration_t _maxBlockingTime = new Duration_t();

   // Declare ##################
   private static long messageCounter = 0;
   private static long messagesSent = 0;
   private static long messagesRcvd = 0;

   private static DomainParticipantFactory factory = null;
   private DomainParticipant participant = null;

   DomainParticipantFactoryQos factory_qos = null;
   DomainParticipantQos participant_qos = null;
   PublisherQos publisher_qos = null;
   Publisher publisher = null;

   // And a data writer to do the work
   DataWriterQos data_writer_qos = null;
   ThroughputDataWriter data_writer = null;
   PublicationMatchedStatus data_writer_status = null;

   InstanceHandle_t data_instanceHandle = null;

   // And a writer to communicate commands
   DataWriterQos command_writer_qos = null;
   PublicationMatchedStatus command_writer_status = null;

   InstanceHandle_t command_instanceHandle = null;

   boolean large_data = false;
   FlowController flow_controller = null;
   FlowControllerProperty_t flow_controller_property = new FlowControllerProperty_t();

   TopicQos data_topic_qos = null;
   Topic data_topic = null;
   TopicQos command_topic_qos = null;
   Topic command_topic = null;

   Throughput data_instance = null;

   // Receiver Declarations

   private DomainParticipant receiver_participant = null;
   DomainParticipantQos receiver_participant_qos = null;
   Subscriber subscriber = null;
   ThroughputDataReader data_reader = null;
   ThroughputListener data_listener = null;

   // End Declare ################

   private int throttlingFactor = 100;
   boolean isReliable = false;
   boolean isAsynch = false;
   String send_topic_name = "Topic_2";
   String receiver_topic_name = "Topic_1";
   ThroughputSubscriber t_put_subscriber;

   private int maxObjectsPerThread = 1024;

   @Override
   public boolean isReadyToRun() {
      boolean isReadyToRun = false;
      PublicationMatchedStatus pubMatch = new PublicationMatchedStatus();
      data_writer.get_publication_matched_status(pubMatch);
      if (pubMatch.current_count > 0) {
         logger.info("Publisher -Subscriber Matched ");
         isReadyToRun = true;
      } else {
         logger.info("Not Matched ");

      }
      return isReadyToRun;
   }

   @Override
   public void sendMessageForThroughput(byte[] message) {
      logger.info("inside send message for throughput");
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

      logger.info("Using throttling factor[" + throttlingFactor + "]");

      String mo = attributes.get("maxObjectsPerThread");

      try {
         maxObjectsPerThread = Integer.parseInt(mo);
      } catch (Exception e) {
         logger.error("Cannot parse max-objects-per-thread.. using default as 1024");
         maxObjectsPerThread = 1024;
      }

      logger.info("Using maxObjectsPerThread as [" + maxObjectsPerThread + "]");

      // send_topic_name = attributes.get("topic");

      logger.info("Using sender topic as [" + send_topic_name + "]");

      // ##########################################
      factory_qos = new DomainParticipantFactoryQos();

      participant_qos = new DomainParticipantQos();
      publisher_qos = new PublisherQos();

      // And a data writer to do the work
      data_writer_qos = new DataWriterQos();
      data_writer_status = new PublicationMatchedStatus();
      data_instanceHandle = InstanceHandle_t.HANDLE_NIL;
      data_topic_qos = new TopicQos();

      // And a writer to communicate commands
      command_writer_qos = new DataWriterQos();
      command_writer_status = new PublicationMatchedStatus();
      command_instanceHandle = InstanceHandle_t.HANDLE_NIL;
      command_topic_qos = new TopicQos();

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
      int samples_per_trigger = 25;
      int max_gather_send_buffers = 16;
      Duration_t ack_wait_time = new Duration_t(60, 0); /* 1 minute */

      if (factory == null) {
         factory = DomainParticipantFactory.get_instance();
         configure_factory_qos(factory_qos, factory);
      }

      configure_participant_qos(participant_qos, factory);
      // Now we can create the 'disabled' participant.
      participant = factory.create_participant(0, participant_qos, null, StatusKind.STATUS_MASK_NONE);
      configure_participant_transport(locator, udpv4TransportProperty, participant);
      participant.enable();
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
      // We need exclusive ownership of this Topic so...
      data_topic_qos.ownership.kind = OwnershipQosPolicyKind.EXCLUSIVE_OWNERSHIP_QOS;

      // And create a Topic with Default QoS and default listener.
      // Note: Ownership is Shared by default.
      data_topic = participant.create_topic(send_topic_name, ThroughputTypeSupport.get_type_name(), data_topic_qos,
               null, StatusKind.STATUS_MASK_NONE);

      /* set up user data */
      data_instance = new Throughput();

      samples_per_trigger = configure_data_writer_qos(data_writer_qos, large_data, max_gather_send_buffers, publisher,
               useMulticast);

      data_writer = (ThroughputDataWriter) publisher.create_datawriter(data_topic, data_writer_qos, null,
               StatusKind.STATUS_MASK_NONE);
      data_writer.enable();
      /*
       * Register Topic instance with data writer. Note: because we are not using keys this will return DDS_HANDLE_NIL
       */
      data_instanceHandle = data_writer.register_instance(data_instance);

      data_instance.data.clear();
      data_instance.data.setMaximum(getMessageLength());
      data_instance.data.addAllByte(new byte[getMessageLength()]);

      // ##############################################################
      incrementCounter();

      logger.info("Init Complete");

      t_put_subscriber = new ThroughputSubscriber();
      t_put_subscriber.init(attributes);

   }

   private synchronized void incrementCounter() {
      counter++;
   }

   private synchronized void decrementCounter() {
      counter--;
   }

   @Override
   public void cleanup() {
      logger.info("Exiting...");
      logger.info("Total sent : " + messagesSent);
      logger.info("total messages received " + messagesRcvd);
      decrementCounter();
      t_put_subscriber.shutdown();
      System.out.println("All threads have finished execution. Shutting down...");
      if (participant != null) {
         participant.delete_contained_entities();
         DomainParticipantFactory.get_instance().delete_participant(participant);
         logger.info("Participant Deleted");
      }

      if (counter == 0) {
         logger.info("Finalizing Factory");
         DomainParticipantFactory.finalize_instance();
         logger.info("Factory Finalized");
      } else {
         logger.info("Counter is non zero ,will not finalize factory");
      }

   }

   private void configure_factory_qos(DomainParticipantFactoryQos factory_qos, DomainParticipantFactory factory) {
      // We need to disable participants so that we can
      // plug in a new/modified transport
      factory.get_qos(factory_qos);
      factory_qos.entity_factory.autoenable_created_entities = false;
      factory_qos.resource_limits.max_objects_per_thread = maxObjectsPerThread;
      factory.set_qos(factory_qos);

   }

   public void configure_participant_qos(DomainParticipantQos participant_qos, DomainParticipantFactory factory) {
      // Configure participant QoS
      factory.get_default_participant_qos(participant_qos);

      participant_qos.event.thread.priority = Thread.MAX_PRIORITY; // 10;
      participant_qos.receiver_pool.thread.priority = Thread.MAX_PRIORITY - 1;

      // Someone may decide to start another publisher so take
      // participant Id from arguments
      participant_qos.wire_protocol.participant_id = counter + 1;

      participant_qos.transport_builtin.mask = 0;// clear all xport first
      participant_qos.transport_builtin.mask |= TransportBuiltinKind.UDPv4;

      // So we can receive large packets.
      participant_qos.receiver_pool.buffer_size = MESSAGE_SIZE_MAX * MAX_SAMPLES;
      participant_qos.event.max_count = MAX_EVENT_COUNT;

   }

   public int configure_participant_transport(String locator, UDPv4Transport.Property_t udpv4TransportProperty,
            DomainParticipant participant) {
      int gather_send_buffer_count_max = 16;
      // Configure built in IPv4 transport to handle large messages
      TransportSupport.get_builtin_transport_property(participant, udpv4TransportProperty);

      udpv4TransportProperty.message_size_max = UDP_SIZE_MAX;
      udpv4TransportProperty.send_socket_buffer_size = udpv4TransportProperty.message_size_max;
      udpv4TransportProperty.recv_socket_buffer_size = udpv4TransportProperty.message_size_max * 2;
      udpv4TransportProperty.multicast_ttl = 1;
      // ONLY one interface permitted for test

      // udpv4TransportProperty.allow_interfaces_list.clear();
      // udpv4TransportProperty.allow_interfaces_list.add("10.88.203.41");

      TransportSupport.set_builtin_transport_property(participant, udpv4TransportProperty);
      gather_send_buffer_count_max = udpv4TransportProperty.gather_send_buffer_count_max;

      return gather_send_buffer_count_max;
   }

   private int configure_data_writer_qos(DataWriterQos data_writer_qos, boolean large_data,
            int max_gather_send_buffers, Publisher publisher, boolean useMulticast) {

      publisher.get_default_datawriter_qos(data_writer_qos);
      // We will own the topic so set the strength as
      // determined by the user.
      data_writer_qos.ownership_strength.value = 5;
      data_writer_qos.resource_limits.initial_samples = 1;

      // use these hard coded value until you use key
      data_writer_qos.resource_limits.max_instances = 1;
      data_writer_qos.resource_limits.initial_instances = data_writer_qos.resource_limits.max_instances;

      if (isAsynch) {
         data_writer_qos.publish_mode.kind = PublishModeQosPolicyKind.ASYNCHRONOUS_PUBLISH_MODE_QOS;
         data_writer_qos.publish_mode.flow_controller_name = "throughput test flow_controller";
      }

      // Unless specified, we will use best effort for this test so...
      if (!isReliable) {
         data_writer_qos.reliability.kind = ReliabilityQosPolicyKind.BEST_EFFORT_RELIABILITY_QOS;
         if (large_data || isAsynch) {
            /*
             * We must queue samples so the asynchronous publishing thread can access them later when tokens become
             * available for sending
             */
            data_writer_qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
            data_writer_qos.resource_limits.max_samples = MAX_SAMPLES;
            data_writer_qos.resource_limits.initial_samples = MAX_SAMPLES;
            data_writer_qos.resource_limits.max_samples_per_instance = MAX_SAMPLES;

            /*
             * max_blocking_time is only relevant when sending asynchronously
             */
            data_writer_qos.reliability.max_blocking_time.sec = 3;
         }
      } else {
         DataWriterQos_setReliableBursty(data_writer_qos, MAX_SAMPLES, getMaxBlockingTime());
         data_writer_qos.protocol.push_on_write = true;

         if (large_data || isAsynch) {
            /*
             * An asynchronous writer coalesces all piggyback HBs into a single HB that gets appended when sending the
             * last asynchronous sample in the queue, so it is ok to request one piggyback HB with every sample.
             */
            data_writer_qos.protocol.rtps_reliable_writer.heartbeats_per_max_samples = MAX_SAMPLES;

            data_writer_qos.reliability.max_blocking_time.sec = 3;
         }

         if (useMulticast) {
            DataWriterQos_setMulticast(data_writer_qos, 32);
         }
      }

      /*
       * When asynchronously writing small samples, we need to trigger the flow controller faster than every 10ms, as
       * the number of samples that can be put on the wire every 10ms greatly exceeds the writer's send queue size.
       * Approach: send message once all gather buffers are used up. Need 1 buffer for RTPS header and >= 2 per issue
       * submessage.
       */
      int samples_per_trigger = (max_gather_send_buffers - 1) / 2;
      /* Trigger at least 4 times per send queue. */
      if (samples_per_trigger > data_writer_qos.resource_limits.max_samples / 4) {
         samples_per_trigger = data_writer_qos.resource_limits.max_samples / 4;
      }
      return samples_per_trigger;
   }

   public Duration_t getMaxBlockingTime() {
      _maxBlockingTime.sec = 0;
      _maxBlockingTime.nanosec = DEFAULT_MAX_BLOCKING_TIME_NS;
      return _maxBlockingTime;
   }

   private void DataWriterQos_setReliableBursty(DataWriterQos qos, int worstBurstInSamples, Duration_t maxBlockingTime) {
      DataWriterQos_setReliableBursty(qos, worstBurstInSamples, maxBlockingTime, 10);
   }

   private void DataWriterQos_setReliableBursty(DataWriterQos qos, int worstBurstInSamples, Duration_t maxBlockingTime,
            int alertReaderWithinThisMs) {
      qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;

      qos.reliability.max_blocking_time.sec = maxBlockingTime.sec;
      qos.reliability.max_blocking_time.nanosec = maxBlockingTime.nanosec;

      qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
      // qos.liveliness.kind = DDS_MANUAL_BY_TOPIC_LIVELINESS_QOS;
      // qos.liveliness.lease_duration.sec = 1;
      // qos.liveliness.lease_duration.nanosec = 400 * NANOSEC_PER_MILLISEC;

      // avoid malloc and pay memory; might have to change policy for large
      // type
      qos.resource_limits.max_samples = worstBurstInSamples;
      qos.resource_limits.initial_samples = worstBurstInSamples;
      // if worst burst == expected burst
      qos.resource_limits.max_samples_per_instance = qos.resource_limits.max_samples;

      // trip high water mark as soon as data starts coming in
      qos.protocol.rtps_reliable_writer.high_watermark = 1;
      /*
       * And stay in fast mode (work harder to resolve stored samples) until all have been delivered.
       */
      qos.protocol.rtps_reliable_writer.low_watermark = 0;

      qos.protocol.rtps_reliable_writer.fast_heartbeat_period.sec = 0;
      qos.protocol.rtps_reliable_writer.fast_heartbeat_period.nanosec = alertReaderWithinThisMs * NANOSEC_PER_MILLISEC;

      // NOTE: piggyback HB irrelevant when push_on_write is turned off
      qos.protocol.rtps_reliable_writer.heartbeats_per_max_samples = worstBurstInSamples / 64;

      // turn off slow HB
      qos.protocol.rtps_reliable_writer.heartbeat_period.sec = 3600 * 24 * 7;

      // don't want to forget the reader even if itsn't responsive for 1 sec
      qos.protocol.rtps_reliable_writer.max_heartbeat_retries = 100;

      qos.protocol.rtps_reliable_writer.min_nack_response_delay.sec = 0;
      qos.protocol.rtps_reliable_writer.min_nack_response_delay.nanosec = 0;
      qos.protocol.rtps_reliable_writer.max_nack_response_delay.sec = 0;
      qos.protocol.rtps_reliable_writer.max_nack_response_delay.nanosec = 0;
   }

   private void DataWriterQos_setMulticast(DataWriterQos qos, int readerQueueSize) {
      DataWriterQos_setMulticast(qos, readerQueueSize, 0, 1);
   }

   private void DataWriterQos_setMulticast(DataWriterQos qos, int readerQueueSize, int nackCoalesceTimeMinInMs,
            int nackCoalesceTimeMaxInMs) {
      // qos.protocol.rtps_reliable_writer.max_bytes_per_nack_response =
      // -readerQueueSize;
      qos.protocol.rtps_reliable_writer.max_bytes_per_nack_response = 32 * 1024;
      qos.protocol.rtps_reliable_writer.min_nack_response_delay.sec = 0;
      qos.protocol.rtps_reliable_writer.min_nack_response_delay.nanosec = nackCoalesceTimeMinInMs
               * NANOSEC_PER_MILLISEC;
      qos.protocol.rtps_reliable_writer.max_nack_response_delay.sec = 0;
      qos.protocol.rtps_reliable_writer.max_nack_response_delay.nanosec = nackCoalesceTimeMaxInMs
               * NANOSEC_PER_MILLISEC;
   }

   @Override
   public void sendMessageForLatency(byte[] message) {
      int len = message.length;
      int exceptionCnt = 0;
      long messageCounter = 0;
      // Set up initial Data
      data_instance.sequence_number = 0;
      setStartTime(new Date());
      int thinkTime = getThinkTimeInMillis();
      while (canContinue()) {
         try {
            for (int l = 0; l < throttlingFactor && canContinue; l++, data_instance.sequence_number++) {
               try {
                  String strString = String.valueOf(System.nanoTime()) + ":" + message;
                  data_instance.data = new ByteSeq(strString.getBytes());
                  data_writer.write(data_instance, data_instanceHandle);
                  messagesSent++;
                  addToStatisticsPool(len);
               } catch (RETCODE_TIMEOUT timeout) {
                  data_instance.sequence_number--;
                  exceptionCnt++;
               }
               try {
                  Thread.sleep(thinkTime);
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }
            }
         } catch (Exception ex) {
            logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = " + ex);
         }

      }

      setEndTime(new Date());
      logger.info("Sent [" + data_instance.sequence_number + "] , Exceptions [" + exceptionCnt + "]");
      logger.info("Sleeping for 8 seconds");
      try {
         Thread.sleep(8000);
      } catch (InterruptedException e1) {
         e1.printStackTrace();
      } finally {
         markTaskComplete();
      }

   }

   public class ThroughputSubscriber {

      final static int NDDS_OVERHEAD = 256; /*
                                             * Conservate estimate to account for overhead
                                             */

      final static int TEST_VERBOSITY_ERRORS = 1;

      final static int TEST_VERBOSITY_WARNINGS = 2;

      final static int TEST_VERBOSITY_MESSAGES = 3;

      final static int THROUGHPUT_TEST_MAX_NODES = 16;

      final static int THROUGHPUT_TEST_DOMAIN_DEFAULT = 0;

      final static int MAX_TEST_SUBSCRIBERS = 16;

      final static int MIN_TEST_DURATION_SEC = 1;

      final static int MAX_COMMAND_LINE_ARGUMENTS = 80;

      // final static int MAX_PEER_LOCATOR_STR_LEN = 128;
      final static int MAX_PEER_PART_IDX = 4; // Allow a maximum of 5

      // participants (0..4) per node.

      final static int MAX_SAMPLES_PER_INSTANCE = 512; // An arbitrary

      // total number of
      // samples

      final static int MAX_INITIAL_SAMPLES = 512; // The initial allocation

      final static int DEFAULT_TEST_DURATION = 10; // 10 seconds

      final static int DEFAULT_TEST_STRENGTH = 5;

      final static int DEFAULT_NUMBER_OF_SUBSCRIBERS = 1;

      final static int DEFAULT_TEST_VERBOSITY = TEST_VERBOSITY_ERRORS;

      final static int DEFAULT_NDDS_VERBOSITY = 1;

      final static int DEFAULT_DEMAND = 10; // Message per write loop

      final static int DEFAULT_PUBLISHER_PARTICIPANT_INDEX = 0;

      final static int DEFAULT_SUBSCRIBER_PARTICIPANT_INDEX = 1;

      final static int DEFAULT_MAX_BLOCKING_TIME_NS = 999000000;

      final static int DEFAULT_FAST_HEARTBEAT_TIME_NS = 1000000;

      final static int TEST_PACKET_OVERHEAD = 8; // 4 Bytes for length of

      // sequence and 4 bytes for
      // sequence number.

      final static int DEFAULT_PACKET_SIZE = (1024 - TEST_PACKET_OVERHEAD);

      final static int NANOSEC_PER_MILLISEC = 1000000;

      final static long NANOSEC_PER_SEC = 1000000000;

      final static int MILLISEC_PER_SEC = 1000;

      final static int MESSAGE_SIZE_MAX = 8192; // 65535

      final static int UDP_SIZE_MAX = 65536;

      final static int SHMEM_SIZE_MAX = 9216;

      final static int MAX_SAMPLES = 100;

      // final static int LOW_WATERMARK = (MAX_SAMPLES - 80); // 20%
      // final static int HIGH_WATERMARK = (MAX_SAMPLES - 30); // 70%
      final static int MAX_EVENT_COUNT = (1024 * 16);

      // final static int _MAX_PATH = 260;
      final static int MAX_ACCEPTABLE_PACKET_LOSS = 4;
      // ##############DECARE

      SubscriberQos subscriber_qos = null;
      // And a data writer to do the work
      DataReaderQos data_reader_qos = null;

      // And a writer to communicate commands
      DataReaderQos command_reader_qos = null;
      TopicQos data_topic_qos = null;
      Topic data_topic = null;
      TopicQos command_topic_qos = null;
      Topic command_topic = null;
      // ############END DECLARE

      boolean isReliable = false;

      private int maxObjectsPerThread = 0;

      public void init(Map<String, String> attributes) {
         String strReliable = attributes.get("isReliable");
         if (strReliable != null && !strReliable.isEmpty()) {
            isReliable = Boolean.parseBoolean(strReliable);
            logger.info("Setting Reliability " + isReliable);
         }

         String mo = attributes.get("maxObjectsPerThread");

         try {
            maxObjectsPerThread = Integer.parseInt(mo);
         } catch (Exception e) {
            System.out.println("Cannot parse max-objects-per-thread.. using default as 1024");
            maxObjectsPerThread = 1024;
         }

         logger.info("Using maxObjectsPerThread as [" + maxObjectsPerThread + "]");

         // receiver_topic_name = attributes.get("topic");
         logger.info("Using server receiver topic as [" + receiver_topic_name + "]");

         logger.info("In Read.. configuring all RTI objects");
         factory_qos = new DomainParticipantFactoryQos();

         receiver_participant_qos = new DomainParticipantQos();
         subscriber_qos = new SubscriberQos();

         // And a data writer to do the work
         data_reader_qos = new DataReaderQos();
         data_topic_qos = new TopicQos();

         // And a writer to communicate commands
         command_reader_qos = new DataReaderQos();
         command_topic_qos = new TopicQos();

         /******************************************************************
          * Plug-in transport variables
          *****************************************************************/
         UDPv4Transport.Property_t udpv4TransportProperty = new UDPv4Transport.Property_t();
         ShmemTransport.Property_t smemTransportProperty = new ShmemTransport.Property_t();
         UDPv6Transport.Property_t udpv6TransportProperty = new UDPv6Transport.Property_t();

         /******************************************************************
          * Other data
          *****************************************************************/

         String locator = null;
         //
         // if (factory == null) {
         // factory = DomainParticipantFactory.get_instance();
         // configure_factory_qos(factory_qos, factory);
         // }

         configure_participant_qos(receiver_participant_qos, factory);
         // Now we can create the 'disabled' participant.
         receiver_participant = factory.create_participant(0, receiver_participant_qos, null,
                  StatusKind.STATUS_MASK_NONE);
         configure_participant_transport(locator, udpv4TransportProperty, smemTransportProperty,
                  udpv6TransportProperty, receiver_participant);

         // Now enable the participant
         receiver_participant.enable();

         receiver_participant.get_default_subscriber_qos(subscriber_qos);
         // And create the subscriber with default QoS and a default listener
         subscriber = receiver_participant.create_subscriber(subscriber_qos, null, StatusKind.STATUS_MASK_NONE);

         /******************************************************************
          * Set up Throughput topic and reader.
          *****************************************************************/
         // Now we register the data topic type with the participant.
         ThroughputTypeSupport.register_type(receiver_participant, ThroughputTypeSupport.get_type_name());

         // Get the default QoS for the Topic
         receiver_participant.get_default_topic_qos(data_topic_qos);
         // There is only one publisher of this Topic so...
         data_topic_qos.ownership.kind = OwnershipQosPolicyKind.EXCLUSIVE_OWNERSHIP_QOS;
         // And create a Topic with QoS and default listener.
         // Note: Ownership is Shared by default.
         data_topic = receiver_participant.create_topic(receiver_topic_name, ThroughputTypeSupport.get_type_name(),
                  data_topic_qos, null, StatusKind.STATUS_MASK_NONE);

         data_listener = new ThroughputListener();
         // data_listener.setVerbosity(args.testVerbosity);

         // Get the default reader QoS and configure to our requirements
         configure_data_reader_qos(data_reader_qos, subscriber);

         data_reader = (ThroughputDataReader) subscriber.create_datareader(data_topic, data_reader_qos, data_listener,
                  StatusKind.STATUS_MASK_ALL);
         data_reader.enable();
         logger.info("Init Complete");
         incrementCounter();
      }

      public void read() {
         logger.info("Read called");
      }

      public void shutdown() {

         logger.info("Shutdown for subscriber called..");
         decrementCounter();
         logger.info("All subscriber threads finished processing. Shutting down...");
         if (receiver_participant != null) {
            try {
               receiver_participant.delete_contained_entities();
               DomainParticipantFactory.get_instance().delete_participant(receiver_participant);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      public void configure_factory_qos(DomainParticipantFactoryQos factory_qos, DomainParticipantFactory factory) {
         // We need to disable participants so that we can plug in a
         // new/modified transport
         factory.get_qos(factory_qos);
         factory_qos.entity_factory.autoenable_created_entities = false;
         factory_qos.resource_limits.max_objects_per_thread = maxObjectsPerThread;
         factory.set_qos(factory_qos);
      }

      public void configure_participant_qos(DomainParticipantQos participant_qos, DomainParticipantFactory factory) {
         // Configure the Participant Quality of Service
         factory.get_default_participant_qos(participant_qos);

         // Someone may decide to start another publisher so take participant
         // Id from arguments
         participant_qos.wire_protocol.participant_id = counter + 2;

         // Ensure that when we receive data it is processed quickly
         participant_qos.event.thread.priority = Thread.MAX_PRIORITY;
         participant_qos.receiver_pool.thread.priority = Thread.MAX_PRIORITY - 1;

         // Turn off Shared Memory only use loop back
         // Comment out to use Shared Memory on local host beware -auto does
         // not run over shared memory

         // So we can receive large packets.
         participant_qos.receiver_pool.buffer_size = MESSAGE_SIZE_MAX * (MAX_SAMPLES * 2);

         participant_qos.transport_builtin.mask = 0;// clear all xport first

         participant_qos.transport_builtin.mask |= TransportBuiltinKind.UDPv4;
      }

      public void configure_participant_transport(String locator, UDPv4Transport.Property_t udpv4TransportProperty,
               ShmemTransport.Property_t smemTransportProperty, UDPv6Transport.Property_t udpv6TransportProperty,
               DomainParticipant participant) {

         // Configure built in IPv4 transport to handle large messages
         TransportSupport.get_builtin_transport_property(participant, udpv4TransportProperty);
         udpv4TransportProperty.message_size_max = UDP_SIZE_MAX;
         udpv4TransportProperty.send_socket_buffer_size = udpv4TransportProperty.message_size_max;
         udpv4TransportProperty.recv_socket_buffer_size = udpv4TransportProperty.message_size_max * 2;

         // udpv4TransportProperty.allow_interfaces_list.clear();
         // udpv4TransportProperty.allow_interfaces_list.add("10.88.203.40");

         TransportSupport.set_builtin_transport_property(participant, udpv4TransportProperty);

      }

      public void configure_data_reader_qos(DataReaderQos data_reader_qos, Subscriber subscriber) {
         subscriber.get_default_datareader_qos(data_reader_qos);

         // use these hard coded value until you use key
         data_reader_qos.resource_limits.max_instances = 1;
         data_reader_qos.resource_limits.initial_instances = data_reader_qos.resource_limits.max_instances;

         data_reader_qos.resource_limits.max_samples_per_instance = 1;
         data_reader_qos.reader_resource_limits.max_samples_per_remote_writer = data_reader_qos.resource_limits.max_samples_per_instance;
         data_reader_qos.resource_limits.max_samples = data_reader_qos.resource_limits.max_samples_per_instance;
         data_reader_qos.resource_limits.initial_samples = data_reader_qos.resource_limits.max_samples_per_instance;

         // Unless specified, we will use best effort for this test so...
         if (isReliable) {
            DataReaderQos_setReliableBursty(data_reader_qos);
         }

         /*
          * if (args.mcast_recv_addr != null) { DataReaderQos_setMulticast(data_reader_qos, args.mcast_recv_addr); }
          */
      }

      private void DataReaderQos_setReliableBursty(DataReaderQos qos) {
         DataReaderQos_setReliableBursty(qos, 1);
      }

      private void DataReaderQos_setReliableBursty(DataReaderQos qos, int remoteWriterCountMax) {
         qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
         qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

         // reader queue can be constant regardless of rate
         int unresolvedSamplePerRemoteWriterMax = 100;

         qos.resource_limits.max_samples = remoteWriterCountMax * unresolvedSamplePerRemoteWriterMax;
         qos.resource_limits.initial_samples = qos.resource_limits.max_samples;

         qos.reader_resource_limits.max_samples_per_remote_writer = qos.resource_limits.initial_samples;
         qos.resource_limits.max_samples_per_instance = qos.resource_limits.initial_samples;

         // the writer probably has more for the reader; ask right away
         qos.protocol.rtps_reliable_reader.min_heartbeat_response_delay.sec = 0;
         qos.protocol.rtps_reliable_reader.min_heartbeat_response_delay.nanosec = 0;
         qos.protocol.rtps_reliable_reader.max_heartbeat_response_delay.sec = 0;
         qos.protocol.rtps_reliable_reader.max_heartbeat_response_delay.nanosec = 0;
      }
   }

   private class ThroughputListener extends DataReaderAdapter {
      ThroughputSeq dataSeq = new ThroughputSeq();
      SampleInfoSeq infoSeq = new SampleInfoSeq();
      private int _packetsReceived;
      private int _packetsLost;
      private long _sequenceNumber;

      public void on_data_available(DataReader reader) {
         ThroughputDataReader dataReader = (ThroughputDataReader) reader;
         messagesRcvd++;

         try {
            dataReader.take(dataSeq, infoSeq, ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                     SampleStateKind.NOT_READ_SAMPLE_STATE, ViewStateKind.ANY_VIEW_STATE,
                     InstanceStateKind.ALIVE_INSTANCE_STATE);

            long recdTimeNano = System.nanoTime();
            for (int i = 0; i < dataSeq.size(); ++i) {
               if (((SampleInfo) infoSeq.get(i)).valid_data) {
                  // if (_sequenceNumber != 0) {
                  String message = ((Throughput) dataSeq.get(i)).data.toString();
                  // if (messagesRcvd % 1000 == 0) {
                  int idx = message.indexOf(":");
                  if (idx != -1) {
                     String sentTime = message.substring(0, idx);
                     long sentTimeNano = Long.parseLong(sentTime);
                     int latency = (int) (recdTimeNano - sentTimeNano);
                     System.out.println("Latency for Message " + latency);
                     // addToStatisticsPool(null,new
                     // Date(),message.length(),latency);
                  }
                  // }
                  _packetsLost += (((Throughput) dataSeq.get(i)).sequence_number - _sequenceNumber);
                  // }
                  _sequenceNumber = ((Throughput) dataSeq.get(i)).sequence_number;
                  // Reset sequence number
                  // addToStatisticsPool(null, 100, new Date());
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
   }
}
