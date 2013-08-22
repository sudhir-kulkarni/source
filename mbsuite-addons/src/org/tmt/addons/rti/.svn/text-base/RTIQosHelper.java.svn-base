package org.tmt.addons.rti;

import java.net.DatagramSocket;
import java.net.SocketException;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.HistoryQosPolicyKind;
import com.rti.dds.infrastructure.PublishModeQosPolicyKind;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.TransportBuiltinKind;
import com.rti.dds.infrastructure.TransportUnicastSettings_t;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.subscription.Subscriber;
import com.rti.ndds.transport.TransportSupport;
import com.rti.ndds.transport.UDPv4Transport;

public class RTIQosHelper {
   final static int MESSAGE_SIZE_MAX = 8192; // 65535
   final static int DEFAULT_MAX_BLOCKING_TIME_NS = 999000000;
   final static int MAX_SAMPLES = 100000;
   final static int NANOSEC_PER_MILLISEC = 1000000;
   final static int MAX_EVENT_COUNT = (1024 * 16);
   final static int UDP_SIZE_MAX = 65536;
   private static Duration_t _maxBlockingTime = new Duration_t();
   public static int bw_limit = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;

   public static void configure_factory_qos(DomainParticipantFactoryQos factory_qos, DomainParticipantFactory factory,
            int maxObjectsPerThread) {
      // We need to disable participants so that we can
      // plug in a new/modified transport
      factory.get_qos(factory_qos);
      factory_qos.entity_factory.autoenable_created_entities = false;
      factory_qos.resource_limits.max_objects_per_thread = maxObjectsPerThread;
      factory.set_qos(factory_qos);
   }

   public static void configure_participant_qos(DomainParticipantQos participant_qos, DomainParticipantFactory factory,
            int participantId) {
      // Configure participant QoS
      factory.get_default_participant_qos(participant_qos);

      participant_qos.event.thread.priority = Thread.MAX_PRIORITY; // 10;
      participant_qos.receiver_pool.thread.priority = Thread.MAX_PRIORITY - 1;

      // Someone may decide to start another publisher so take
      // participant Id from arguments
      participant_qos.wire_protocol.participant_id = participantId;

      participant_qos.transport_builtin.mask = 0;// clear all xport first
      participant_qos.transport_builtin.mask |= TransportBuiltinKind.UDPv4;

      // So we can receive large packets.
      participant_qos.receiver_pool.buffer_size = MESSAGE_SIZE_MAX * MAX_SAMPLES;
      participant_qos.event.max_count = MAX_EVENT_COUNT;

   }

   public static int configure_participant_transport(String locator, UDPv4Transport.Property_t udpv4TransportProperty,
            DomainParticipant participant) {
      int gather_send_buffer_count_max = 16;
      // Configure built in IPv4 transport to handle large messages
      TransportSupport.get_builtin_transport_property(participant, udpv4TransportProperty);

      udpv4TransportProperty.message_size_max = UDP_SIZE_MAX;
      udpv4TransportProperty.send_socket_buffer_size = udpv4TransportProperty.message_size_max;
      udpv4TransportProperty.recv_socket_buffer_size = udpv4TransportProperty.message_size_max * 2;
      udpv4TransportProperty.multicast_ttl = 1;
      // ONLY one interface permitted for test

      TransportSupport.set_builtin_transport_property(participant, udpv4TransportProperty);
      gather_send_buffer_count_max = udpv4TransportProperty.gather_send_buffer_count_max;

      return gather_send_buffer_count_max;
   }

   public static int configure_data_writer_qos(DataWriterQos data_writer_qos, boolean large_data,
            int max_gather_send_buffers, Publisher publisher, boolean useMulticast, boolean isAsynch,
            boolean isReliable, int counter) {

      publisher.get_default_datawriter_qos(data_writer_qos);
      // We will own the topic so set the strength as
      // determined by the user.
      data_writer_qos.ownership_strength.value = 5;
      data_writer_qos.resource_limits.initial_samples = 1;

      TransportUnicastSettings_t setting = new TransportUnicastSettings_t();
      int myport = findPort();
      setting.receive_port = myport;
      System.out.println("Recieve port for this data writer = " + setting.receive_port);
      data_writer_qos.unicast.value.clear();
      data_writer_qos.unicast.value.add(setting);

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

   public static Duration_t getMaxBlockingTime() {
      _maxBlockingTime.sec = 0;
      _maxBlockingTime.nanosec = DEFAULT_MAX_BLOCKING_TIME_NS;
      return _maxBlockingTime;
   }

   private static void DataWriterQos_setReliableBursty(DataWriterQos qos, int worstBurstInSamples,
            Duration_t maxBlockingTime) {
      DataWriterQos_setReliableBursty(qos, worstBurstInSamples, maxBlockingTime, 10);
   }

   private static void DataWriterQos_setReliableBursty(DataWriterQos qos, int worstBurstInSamples,
            Duration_t maxBlockingTime, int alertReaderWithinThisMs) {
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

   private static void DataWriterQos_setMulticast(DataWriterQos qos, int readerQueueSize) {
      DataWriterQos_setMulticast(qos, readerQueueSize, 0, 1);
   }

   private static void DataWriterQos_setMulticast(DataWriterQos qos, int readerQueueSize, int nackCoalesceTimeMinInMs,
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

   public static void configure_data_reader_qos(DataReaderQos data_reader_qos, Subscriber subscriber,
            boolean isReliable, int counter) {
      subscriber.get_default_datareader_qos(data_reader_qos);

      TransportUnicastSettings_t setting = new TransportUnicastSettings_t();
      System.out.print("Searching for available ports to use");
      int myport = findPort();
      setting.receive_port = myport;
      System.out.println("Recieve port for this data reader = " + setting.receive_port);
      data_reader_qos.unicast.value.clear();
      data_reader_qos.unicast.value.add(setting);

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

   public static int findPort() {
      int myport=0;
      int i=0;
      while (true) {
         System.out.print(".");
         i++;
         myport = 7400 + 88 + i;
         DatagramSocket s = null;
         try {
            s = new DatagramSocket();
            myport = s.getLocalPort();
            System.out.print("Found [" + myport+"]" );
            s.close();
            break;
         } catch (SocketException e) {
            e.printStackTrace();
         } finally {
            if (s != null)
               s.close();
         }
      }
      return myport;
   }

   private static void DataReaderQos_setReliableBursty(DataReaderQos qos) {
      DataReaderQos_setReliableBursty(qos, 100);
   }

   private static void DataReaderQos_setReliableBursty(DataReaderQos qos, int remoteWriterCountMax) {
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
