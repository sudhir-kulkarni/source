package org.tmt.addons.rti;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmt.addons.rti.latency.Latency;
import org.tmt.addons.rti.latency.LatencyDataReader;
import org.tmt.addons.rti.latency.LatencyDataWriter;
import org.tmt.addons.rti.latency.LatencySeq;
import org.tmt.addons.rti.latency.LatencyTypeSupport;
import org.tmt.addons.rti.latency.MAX_DATA_SEQUENCE_LENGTH;

import com.persistent.bcsuite.base.PublisherBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.HistoryQosPolicyKind;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.TransportUnicastSettings_t;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.RequestedIncompatibleQosStatus;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.SubscriptionMatchedStatus;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;
import com.rti.ndds.transport.TransportSupport;
import com.rti.ndds.transport.UDPv4Transport;

public class ThroughputLatencyTestPublisher extends PublisherBase {
   private static Logger logger = Logger.getLogger(ThroughputLatencyTestPublisher.class);
   public static final int RTI_DDS_OVERHEAD = 512;
   public static final int FINAL_SN = -1;
   public int num_iterations = 100000, min_size = 16, maxSize = 8192;
   // Create data sample for writing
   Latency instance = new Latency();

   DomainParticipantFactoryQos factory_qos = new DomainParticipantFactoryQos();
   private static DomainParticipantFactory factory = null;
   DomainParticipantQos sender_participant_qos = new DomainParticipantQos();
   DomainParticipantQos receiver_participant_qos = new DomainParticipantQos();
   private static DomainParticipant senderParticipant = null;
   private static DomainParticipant receiverParticipant = null;
   Subscriber subscriber = null;
   Publisher publisher = null;
   Topic data_topic = null, echo_topic = null;
   NewLatencyListener listener = null;
   DataReaderQos reader_qos = new DataReaderQos();
   LatencyDataReader reader = null;
   DataWriterQos writer_qos = new DataWriterQos();
   LatencyDataWriter writer = null;
   InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
   public static byte num_subscribers = 1;
   private int maxObjectsPerThread = 0;
   String topicName = null;
   private static int counter = 0;
   private int throttlingFactor = 0;
   private int latencyCaptureWindow;

   @Override
   public void init(Map<String, String> attributes) {
      super.init(attributes);

      String tf = attributes.get("throttlingFactor");

      try {
         throttlingFactor = Integer.parseInt(tf);
      } catch (Exception e) {
         logger.error("Cannot parse throttling factor.. using default as 100");
         throttlingFactor = 100;
      }

      logger.info("Using throttling factor[" + throttlingFactor + "]");

      String lc = attributes.get("latencyCaptureWindow");
      try {
         latencyCaptureWindow = Integer.parseInt(lc);
      } catch (Exception e) {
         logger.error("Cannot parse latencyCaptureWindow.. using default as 100");
         latencyCaptureWindow = 100;
      }

      logger.info("Using latencyCaptureWindow as [" + latencyCaptureWindow + "]");

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

      // Need to change default plugin property
      if (factory == null) {
         factory = DomainParticipantFactory.get_instance();
         factory.get_qos(factory_qos);
         factory_qos.entity_factory.autoenable_created_entities = false;
         factory_qos.resource_limits.max_objects_per_thread = maxObjectsPerThread;
         factory.set_qos(factory_qos);
      }
      // --- Create participant --- //
      factory.get_default_participant_qos(sender_participant_qos);

      sender_participant_qos.wire_protocol.participant_id = counter;
      sender_participant_qos.resource_limits.writer_user_data_max_length = 128;
      sender_participant_qos.receiver_pool.buffer_size = MAX_DATA_SEQUENCE_LENGTH.VALUE + 8 + RTI_DDS_OVERHEAD;
      sender_participant_qos.transport_builtin.mask = 1;
      sender_participant_qos.discovery_config.participant_liveliness_assert_period.sec = 61;
      sender_participant_qos.discovery_config.participant_liveliness_lease_duration.sec = 128;

      UDPv4Transport.Property_t property = new UDPv4Transport.Property_t();
      if (senderParticipant == null) {
         senderParticipant = factory.create_participant(0, sender_participant_qos, null, // listener
                  StatusKind.STATUS_MASK_NONE);
         
         TransportSupport.get_builtin_transport_property(senderParticipant, property);

         property.message_size_max = sender_participant_qos.receiver_pool.buffer_size;
         property.send_socket_buffer_size = property.message_size_max;
         property.recv_socket_buffer_size = 2 * property.send_socket_buffer_size;
         property.multicast_ttl = 1;

         TransportSupport.set_builtin_transport_property(senderParticipant, property);
         // Enable to start Discovery
         senderParticipant.enable();

      }
      
      factory.get_default_participant_qos(receiver_participant_qos);

      receiver_participant_qos.wire_protocol.participant_id = counter+20;
      receiver_participant_qos.resource_limits.writer_user_data_max_length = 128;
      receiver_participant_qos.receiver_pool.buffer_size = MAX_DATA_SEQUENCE_LENGTH.VALUE + 8 + RTI_DDS_OVERHEAD;
      receiver_participant_qos.transport_builtin.mask = 1;
      receiver_participant_qos.discovery_config.participant_liveliness_assert_period.sec = 61;
      receiver_participant_qos.discovery_config.participant_liveliness_lease_duration.sec = 128;
      if(receiverParticipant == null)
      {
         receiverParticipant = factory.create_participant(0, receiver_participant_qos, null, // listener
                  StatusKind.STATUS_MASK_NONE);
         TransportSupport.set_builtin_transport_property(receiverParticipant, property);
   
         // Enable to start Discovery
         receiverParticipant.enable();
      }

      subscriber = receiverParticipant.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null, // listener
               StatusKind.STATUS_MASK_NONE);

      publisher = senderParticipant.create_publisher(DomainParticipant.PUBLISHER_QOS_DEFAULT, null, // listener
               StatusKind.STATUS_MASK_NONE);

      // --- Create topics --- //
      // Register type before creating topic
      String typeName = LatencyTypeSupport.get_type_name();
      LatencyTypeSupport.register_type(senderParticipant, typeName);
      LatencyTypeSupport.register_type(receiverParticipant, typeName);
      // To customize topic QoS, use
      // participant.get_default_topic_qos() instead
      data_topic = senderParticipant.create_topic(topicName + "-DataTopic", typeName,
               DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
               StatusKind.STATUS_MASK_NONE);
      echo_topic = receiverParticipant.create_topic(topicName + "-EchoTopic", typeName,
               DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
               StatusKind.STATUS_MASK_NONE);

      // data writer
      publisher.get_default_datawriter_qos(writer_qos);

      writer_qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
      // writer_qos.history.depth = 1;

      writer_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
      writer_qos.reliability.max_blocking_time.sec = 2;
      writer_qos.reliability.max_blocking_time.nanosec = 0;
      writer_qos.resource_limits.initial_samples = writer_qos.resource_limits.max_samples = writer_qos.resource_limits.max_samples_per_instance = 3;

      writer_qos.protocol.rtps_reliable_writer.fast_heartbeat_period.sec = 0;
      writer_qos.protocol.rtps_reliable_writer.fast_heartbeat_period.nanosec = 1 * 1000000; // 1 ms
      writer_qos.protocol.rtps_reliable_writer.heartbeat_period.sec = 3600 * 24 * 7; // 1 week

      // Because the queue is 1, want to piggyback HB
      // w/ every sample.
      // This may be a duplicate HB,
      // but since the send is low, it's OK
      writer_qos.protocol.rtps_reliable_writer.heartbeats_per_max_samples = (writer_qos.resource_limits.max_samples > 1) ? writer_qos.resource_limits.max_samples
               : 100000000;

      writer = (LatencyDataWriter) publisher.create_datawriter(data_topic, writer_qos, null, // listener
               StatusKind.STATUS_MASK_NONE);
      // --- Create reader --- //
      listener = new NewLatencyListener(num_iterations);

      subscriber.get_default_datareader_qos(reader_qos);
      TransportUnicastSettings_t setting = new TransportUnicastSettings_t();
      System.out.print("Searching for available ports to use");
      int myport = RTIQosHelper.findPort();
      setting.receive_port = myport;
      System.out.println("Recieve port for this data reader = " + setting.receive_port);
      reader_qos.unicast.value.clear();
      reader_qos.unicast.value.add(setting);
      
      reader_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
      reader_qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
      reader_qos.protocol.rtps_reliable_reader.max_heartbeat_response_delay.sec = 0;
      reader_qos.protocol.rtps_reliable_reader.max_heartbeat_response_delay.nanosec = 0;

      reader = (LatencyDataReader) subscriber.create_datareader(echo_topic, reader_qos, listener,
               (StatusKind.DATA_AVAILABLE_STATUS | StatusKind.REQUESTED_INCOMPATIBLE_QOS_STATUS));

      // For data type that has key, if the same instance is going to be
      // written multiple times, initialize the key here
      // and register the keyed instance prior to writing
      // instance_handle_reg = writer.register_instance(instance);
      InstanceHandle_t instance_handle_reg = InstanceHandle_t.HANDLE_NIL;

      counter++;
   }

   @Override
   public boolean isReadyToRun() {
      // wait for the requisite number of readers to appear
      PublicationMatchedStatus matched_status = new PublicationMatchedStatus();
      writer.get_publication_matched_status(matched_status);
      if (matched_status.current_count > 0) {
         System.out.println("Data Writer Publication Matched");
      }
      System.out.println(" Data Writer Publication  Not Matched");

      SubscriptionMatchedStatus readerMatchedStatus = new SubscriptionMatchedStatus();
      reader.get_subscription_matched_status(readerMatchedStatus);
      if (readerMatchedStatus.current_count > 0) {
         System.out.println("Data Reader Subscription Matched");
         return true; // found everybody, can move on
      }
      System.out.println("Data Reader Subscription NOT Matched");

      return false;
   }

   @Override
   public void sendMessageForThroughput(byte[] message) {
      // TODO Auto-generated method stub

   }

   /*
    * @Override public void sendMessageForLatency(byte[] message){ try{ instance.data.clear();
    * instance.data.setMaximum(getMessageLength()); instance.data.addAllByte(message); // set the subscribers know who
    * should echo (the last one) instance.data.setByte(0, num_subscribers);
    * 
    * listener.start_one_round(getMessageLength()); setStartTime(new Date()); for (instance.sequence_number = 1;
    * instance.sequence_number <= throttlingFactor && canContinue;) {
    * listener.start_one_message(instance.sequence_number); writer.write(instance, instance_handle); // -- Write --
    * addToStatisticsPool(getMessageLength()); // block up to 1 s for reply if (listener.wait_for_reply(1000)) {
    * ++instance.sequence_number; } else { // warn, sleep, and retry System.out.println("Did not get reply at sn " +
    * instance.sequence_number); Thread.sleep(10); } } }catch(Exception ex) { ex.printStackTrace(); } setEndTime(new
    * Date()); instance.sequence_number = FINAL_SN; instance.data.setMaximum(20);
    * listener.start_one_message(instance.sequence_number); writer.write(instance, instance_handle);
    * 
    * markTaskComplete(); }
    */

   @Override
   public void sendMessageForLatency(byte[] message) {
      setStartTime(new Date());
      instance.data.clear();
      instance.data.setMaximum(getMessageLength());
      instance.data.addAllByte(message);
      // set the subscribers know who should echo (the last one)
      instance.data.setByte(0, num_subscribers);

      while (canContinue) {
         try {
            // listener.start_one_round(getMessageLength());
            setStartTime(new Date());
            for (instance.sequence_number = 1; instance.sequence_number <= throttlingFactor && canContinue;instance.sequence_number++) {
               instance.sentTime = System.nanoTime();
               logger.info("seqnumber = " + instance.sequence_number + ", sent time = " + instance.sentTime);
               writer.write(instance, instance_handle); // -- Write --
               addToStatisticsPool(getMessageLength());
            }
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }
      setEndTime(new Date());
      instance.sequence_number = FINAL_SN;
      //instance.data.setMaximum(20);
      // listener.start_one_message(instance.sequence_number);
      writer.write(instance, instance_handle);

      markTaskComplete();
   }

   @Override
   public void cleanup() {
      counter--;
      
      if(counter == 0)
      {
         logger.info("Counter is zero, destroying recieverParticipant");
         if (senderParticipant != null) {
            senderParticipant.delete_contained_entities();
   
            DomainParticipantFactory.TheParticipantFactory.delete_participant(senderParticipant);
         }
         logger.info("Counter is zero, destroying senderParticipant");
         if(receiverParticipant != null)
         {
            receiverParticipant.delete_contained_entities();
            
            DomainParticipantFactory.TheParticipantFactory.delete_participant(receiverParticipant);
         }
         // NDDS provides finalize_instance() method for people who want to
         // release memory used by the participant factory singleton.
         // Uncomment the following block of code for clean destruction of
         // the participant factory singleton.
         logger.info("Counter is zero, finalizing instance");
            DomainParticipantFactory.finalize_instance();
      }
      else
      {
         logger.info("Counter is non zero, cannot destroy participants");
      }
   }

   private class NewLatencyListener extends DataReaderAdapter {
      private int _num_iterations, _expected_sn=1, _received_sn, _message_size;
      private long _time_sent, _clock_overhead;
      public Logger logger;
      public ArrayList<Long> roundtrip_time_array;

      LatencySeq data_seq = new LatencySeq();
      SampleInfoSeq info_seq = new SampleInfoSeq();

      private int msgCounter = 0;

      public NewLatencyListener(int num_iterations) {
         _num_iterations = num_iterations;
         roundtrip_time_array = new ArrayList<Long>();
         roundtrip_time_array.ensureCapacity(num_iterations);

         long start_time, finish_time = 0;
         start_time = System.nanoTime();
         for (int i = 0; i < 16; ++i) {
            finish_time = System.nanoTime();
         }
         _clock_overhead = (finish_time - start_time) / 16;

         logger = Logger.getLogger(NewLatencyListener.class);
      }

      // @brief for debugging
      public void on_requested_incompatible_qos(DataReader reader, RequestedIncompatibleQosStatus status) {
         System.out.println("Incompatible QoS " + status.last_policy_id);
      }

      public synchronized void on_data_available(DataReader reader) {
         LatencyDataReader latency_reader = (LatencyDataReader) reader;
         try {
            latency_reader.take(data_seq, info_seq, ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                     SampleStateKind.ANY_SAMPLE_STATE, ViewStateKind.ANY_VIEW_STATE,
                     InstanceStateKind.ANY_INSTANCE_STATE);

            long time_received = System.nanoTime();
            for (int i = 0; i < data_seq.size(); ++i) {
               msgCounter++;
               SampleInfo info = (SampleInfo) info_seq.get(i);
               if (info.valid_data) {
                  Latency msg = (Latency) data_seq.get(i);
                  _received_sn = msg.sequence_number;
                  if (_received_sn == _expected_sn) {
                     logger.info("Echoed back - seqnumber = " + _received_sn + ", sent time = " + msg.sentTime);
                     long roundtrip_time = time_received - msg.sentTime - _clock_overhead;
                     if (msgCounter > latencyCaptureWindow) {
                        addLatencyStatistics(String.valueOf(msgCounter), roundtrip_time);
                        msgCounter = 0;
                     }
                     
                     //notify(); // let the main thread send another packet
                  } else {
                     System.out.println("**********ERROR: " + "Received SN " + msg.sequence_number + "!= expected "
                              + _expected_sn);
                  }
                  if (msg.sequence_number > _num_iterations) {
                     System.out.println("**********ERROR: " + "Received SN > expected " + _num_iterations);
                  }
               }
               _expected_sn++;
            }
         } catch (RETCODE_NO_DATA no_data) { // No data to process
         } finally {
            latency_reader.return_loan(data_seq, info_seq);
         }
      }

   }
}
