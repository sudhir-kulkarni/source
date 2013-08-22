package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.PublisherBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.BatchQosPolicy;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.HistoryQosPolicyKind;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PublishModeQosPolicyKind;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.TransportBuiltinKind;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.FlowController;
import com.rti.dds.publication.FlowControllerProperty_t;
import com.rti.dds.publication.Publisher;
import com.rti.dds.publication.PublisherQos;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;
import com.rti.ndds.transport.TransportSupport;
import com.rti.ndds.transport.UDPv4Transport;

/**
 * 
 * @author deepti_nagarkar
 * 
 */

public class RTIPublisher extends PublisherBase {

	private static DomainParticipantFactory factory;
	private static DomainParticipant participant;
	private Publisher publisher;
	private Topic topic;
	private StringDataWriter dataWriter;
	FlowController flow_controller = null;
	FlowControllerProperty_t flow_controller_property = new FlowControllerProperty_t();

	private String topicName;
	public static Logger logger = Logger.getLogger(RTIPublisher.class);
	public int bw_limit = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
	static final int MESSAGE_SIZE_MAX = 8192; // 65535
	static final int UDP_SIZE_MAX = 65536;
	static final int MAX_SAMPLES = 1024;
	static final int MAX_EVENT_COUNT = (1024 * 16);
	private final int MAX_PARTICIPANTS_PER_FACTORY = 9;
	final static int NANOSEC_PER_MILLISEC = 1000000;

	int samples_per_trigger = 25, max_gather_send_buffers = 16;
	boolean asyncFlag = false;
	boolean batchModeFlag = false;
	boolean isReliable = false;
	private final Duration_t threeSecond = new Duration_t(3, 0);
	private static int counter = 0;

	@Override
	public void init(Map<String, String> attributes) {
		super.init(attributes);
		String strAsync = attributes.get("asyncFlag");
		if (strAsync != null && !strAsync.isEmpty()) {
			asyncFlag = Boolean.parseBoolean(strAsync);
		}

		String strBatch = attributes.get("batchModeFlag");
		if (strBatch != null && !strBatch.isEmpty()) {
			batchModeFlag = Boolean.parseBoolean(strBatch);
		}
		String strReliable = attributes.get("isReliable");
		if (strReliable != null && !strReliable.isEmpty()) {
			isReliable = Boolean.parseBoolean(strReliable);
			System.out.println("Setting Reliability " + isReliable);
		}
		if (factory == null) {
			initFactory();
		}
		if (participant == null) {
			initDomainParticipant();
		}
		if (asyncFlag) {
			// System.out.println("Using Aysnc Mode ");
			initFlowController();
		}
		initPublisher();
		topicName = attributes.get("topic");
		// Create a topic from the message
		// if(topic ==null)
		createTopic(topicName);
		// Create the datawriter
		createDataWriter();
		System.out.println("Ready to write data.");
		System.out.print("Press CTRL+C to terminate\n\n");
		incrementCounter();
	}

	private void initFlowController() {
		/* Configure flow controller Quality of Service */
		configure_flow_controller_property(flow_controller_property);

		/* Create flowcontroller */
		flow_controller = participant.create_flowcontroller(
				"throughput test flow_controller", flow_controller_property);

	}

	private void configure_flow_controller_property(
			FlowControllerProperty_t flow_controller_property) {

		participant
				.get_default_flowcontroller_property(flow_controller_property);

		/*
		 * Approach: send small bursts as fast as possible (as opposed to
		 * sending larger bursts, but slower)
		 */
		// if (large_data && args.bw_limit > 0) {
		flow_controller_property.token_bucket.tokens_added_per_period = (int) ((float) bw_limit * 1000000.0 / 8.0 / 100.0 / 1024.0);
		// } else {
		// flow_controller_property.token_bucket.tokens_added_per_period =
		// ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
		// }
		flow_controller_property.token_bucket.max_tokens = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
		flow_controller_property.token_bucket.tokens_leaked_per_period = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
		// tenMilliSec
		flow_controller_property.token_bucket.period.sec = 0;
		flow_controller_property.token_bucket.period.nanosec = 10 * 1000000;
		flow_controller_property.token_bucket.bytes_per_token = 1024;
		/* minimum value */
	}

	private void initPublisher() {
		PublisherQos publisher_qos = new PublisherQos();
		participant.get_default_publisher_qos(publisher_qos);
		 publisher_qos.asynchronous_publisher.thread.priority =
		 Thread.MAX_PRIORITY - 1;
		publisher = participant.create_publisher(publisher_qos, null,
				StatusKind.STATUS_MASK_NONE);
		publisher.enable();

	}

	private synchronized void initDomainParticipant() {
		Random random = new Random();
		DomainParticipantQos participant_qos = new DomainParticipantQos();
		factory.get_default_participant_qos(participant_qos);
		int participantId = random.nextInt(80);
		System.out.println("Creating participant with Id " + participantId);
		 configure_participant_qos(participant_qos, factory, participantId);

		participant = factory.create_participant(0, // Domain ID = 0
				participant_qos, null, // listener
				StatusKind.STATUS_MASK_NONE);
		 UDPv4Transport.Property_t udpv4TransportProperty = new
		 UDPv4Transport.Property_t();
		 configure_participant_transport(udpv4TransportProperty, participant);
		if (participant == null) {
			System.out.println("Unable to create domain participant");
			return;
		}

		participant.enable();

	}

	private synchronized void initFactory() {
		factory = DomainParticipantFactory.get_instance();
		DomainParticipantFactoryQos factory_qos = new DomainParticipantFactoryQos();
		configure_factory_qos(factory_qos, factory);
	}

	private void configure_factory_qos(DomainParticipantFactoryQos factory_qos,
			DomainParticipantFactory factory) {
		// We need to disable participants so that we can
		// plug in a new/modified transport
		factory.get_qos(factory_qos);
		factory_qos.entity_factory.autoenable_created_entities = false;
		// factory_qos.resource_limits.max_objects_per_thread =
		// MAX_PARTICIPANTS_PER_FACTORY;
		factory.set_qos(factory_qos);
	}

	private void configuredata_writer_QOS(DataWriterQos data_writer_qos,
			Publisher publisher) {
		publisher.get_default_datawriter_qos(data_writer_qos);
		Duration_t _maxBlockingTime = new Duration_t(
				Duration_t.DURATION_INFINITE_SEC, 0);

		boolean no_push_on_write = false;
		// We will own the topic so set the strength as
		// determined by the user.
		// data_writer_qos.ownership_strength.value = args.strength;
		data_writer_qos.resource_limits.initial_samples = 1;

		// use these hard coded value until you use key
		data_writer_qos.resource_limits.max_instances = 10;
		data_writer_qos.resource_limits.initial_instances = data_writer_qos.resource_limits.max_instances;

		if (!isReliable) {
			data_writer_qos.reliability.kind = ReliabilityQosPolicyKind.BEST_EFFORT_RELIABILITY_QOS;
		} else {
			data_writer_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
			// DataWriterQos_setReliableBursty(data_writer_qos,
			// MAX_SAMPLES,_maxBlockingTime);
//			data_writer_qos.protocol.push_on_write = !no_push_on_write;
			data_writer_qos.history.kind = 
			    HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
		}
		if (asyncFlag) {
			data_writer_qos.publish_mode.kind = PublishModeQosPolicyKind.ASYNCHRONOUS_PUBLISH_MODE_QOS;
			data_writer_qos.publish_mode.flow_controller_name = "throughput test flow_controller";
		}
		// for batch mode
		if (batchModeFlag) {
			BatchQosPolicy batch_qos = data_writer_qos.batch;
			batch_qos.enable = true;
			batch_qos.max_data_bytes = MESSAGE_SIZE_MAX;
			// batch_qos.max_flush_delay = threeSecond;
			// batch_qos. = threeSecond;
		}
		// data_writer_qos.publish_mode.kind =
		// PublishModeQosPolicyKind.SYNCHRONOUS_PUBLISH_MODE_QOS;
		// for multicast
	}

	private static void DataWriterQos_setReliableBursty(DataWriterQos qos,
			int worstBurstInSamples, Duration_t maxBlockingTime) {
		DataWriterQos_setReliableBursty(qos, worstBurstInSamples,
				maxBlockingTime, 10);
	}

	private static void DataWriterQos_setReliableBursty(DataWriterQos qos,
			int worstBurstInSamples, Duration_t maxBlockingTime,
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
		 * And stay in fast mode (work harder to resolve stored samples) until
		 * all have been delivered.
		 */
		qos.protocol.rtps_reliable_writer.low_watermark = 0;

		qos.protocol.rtps_reliable_writer.fast_heartbeat_period.sec = 0;
		qos.protocol.rtps_reliable_writer.fast_heartbeat_period.nanosec = alertReaderWithinThisMs
				* NANOSEC_PER_MILLISEC;

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

	private void configure_participant_qos(
			DomainParticipantQos participant_qos,
			DomainParticipantFactory factory, int participantId) {

		// Configure participant QoS
		factory.get_default_participant_qos(participant_qos);

		participant_qos.event.thread.priority = Thread.MAX_PRIORITY; // 10;
		participant_qos.receiver_pool.thread.priority = Thread.MAX_PRIORITY - 1;

		// Someone may decide to start another publisher assign different
		// participant Id
		participant_qos.wire_protocol.participant_id = participantId;
		participant_qos.transport_builtin.mask = 0;// clear all xport first
		participant_qos.transport_builtin.mask |= TransportBuiltinKind.UDPv4;

		participant_qos.receiver_pool.buffer_size = MESSAGE_SIZE_MAX	* MAX_SAMPLES;
		participant_qos.event.max_count = MAX_EVENT_COUNT;

	}

	private int configure_participant_transport(
			UDPv4Transport.Property_t udpv4TransportProperty,
			DomainParticipant participant) {
		int gather_send_buffer_count_max = 16;
		// if (args.transportToUse() == TransportBuiltinKind.UDPv4) {
		// Configure built in IPv4 transport to handle large messages
		TransportSupport.get_builtin_transport_property(participant,
				udpv4TransportProperty);

		udpv4TransportProperty.message_size_max = UDP_SIZE_MAX;
		udpv4TransportProperty.send_socket_buffer_size = udpv4TransportProperty.message_size_max;
		udpv4TransportProperty.recv_socket_buffer_size = udpv4TransportProperty.message_size_max * 2;
		udpv4TransportProperty.multicast_ttl = 1;// args.multicast_ttl;
		// ONLY one interface permitted for test

		// String locator ="10.88.203.40";
		// if(locator != null && !locator.equals("")) {
		// udpv4TransportProperty.allow_interfaces_list.clear();
		// udpv4TransportProperty.allow_interfaces_list.add(locator);
		// }

		TransportSupport.set_builtin_transport_property(participant,
				udpv4TransportProperty);
		gather_send_buffer_count_max = udpv4TransportProperty.gather_send_buffer_count_max;

		return gather_send_buffer_count_max;
	}

	private int configureAsynschronousQoS(DataWriterQos data_writer_qos) {

		data_writer_qos.publish_mode.kind = PublishModeQosPolicyKind.ASYNCHRONOUS_PUBLISH_MODE_QOS;
		data_writer_qos.publish_mode.flow_controller_name = "throughput test flow_controller";
		/*
		 * We must queue samples so the asynchronous publishing thread can
		 * access them later when tokens become available for sending
		 */
		data_writer_qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
		data_writer_qos.resource_limits.max_samples = MAX_SAMPLES;
		data_writer_qos.resource_limits.initial_samples = MAX_SAMPLES;
		data_writer_qos.resource_limits.max_samples_per_instance = MAX_SAMPLES;
		/*
		 * max_blocking_time is only relevant when sending asynchronously
		 */
		data_writer_qos.reliability.max_blocking_time.sec = 3;

		/*
		 * When asynchronously writing small samples, we need to trigger the
		 * flow controller faster than every 10ms, as the number of samples that
		 * can be put on the wire every 10ms greatly exceeds the writer's send
		 * queue size. Approach: send message once all gather buffers are used
		 * up. Need 1 buffer for RTPS header and >= 2 per issue submessage.
		 */
		samples_per_trigger = (max_gather_send_buffers - 1) / 2;
		/* Trigger at least 4 times per send queue. */
		if (samples_per_trigger > data_writer_qos.resource_limits.max_samples / 4) {
			samples_per_trigger = data_writer_qos.resource_limits.max_samples / 4;
		}
		return samples_per_trigger;
	}

	public void sendMessage(byte[] message) {
		try {
			// while (true) {
			while (canContinue) {
				// System.out.print("Sending the message> ");
				String toWrite = new String(message);

				System.out.println(toWrite.substring(0, 10));
				dataWriter.write(toWrite, InstanceHandle_t.HANDLE_NIL);
			}
		} catch (RETCODE_ERROR e) {
			// This exception can be thrown from DDS write operation
			e.printStackTrace();
		}
	}

	@Override
	public void cleanup() {
		System.out.println("Exiting...");
		markTaskComplete();
		decrementCounter();
		if (counter == 0) {
			System.out
					.println("All threads have finished execution. Shutting down...");
			if (participant != null) {
				participant.delete_contained_entities();
				DomainParticipantFactory.get_instance().delete_participant(
						participant);
			}
		}
	}

	private static synchronized void incrementCounter() {
		counter++;
	}

	private static synchronized void decrementCounter() {
		counter--;
	}

	/**
	 * Create a topic from the message generated
	 */
	private void createTopic(String topicName) {
		System.out.println("Creating Topic " + topicName);
		Topic tempTopic = null;
		try {
			tempTopic = participant.create_topic(topicName,
					StringTypeSupport.get_type_name(),
					DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
					StatusKind.STATUS_MASK_NONE);
		} catch (RETCODE_ERROR error) {
			logger.debug("Cannot Create topic. Topic already exists ");
		}
		if (tempTopic == null) {
			logger.debug("Topic already exists. Getting handle to topic");
			topic = participant.find_topic(topicName, threeSecond);
			logger.debug("Discovered Topic " + topic.get_name());
			System.out.println("Discovered Topic " + topic.get_name());
		} else {
			topic = tempTopic;
			topic.enable();
		}
	}

	private void createDataWriter() {
		logger.debug("Creating the Data Writer");
		// Create the data writer using the default publisher
		DataWriterQos datawriter_qos = new DataWriterQos();
		 configuredata_writer_QOS(datawriter_qos, publisher);
		if (asyncFlag) {
			configureAsynschronousQoS(datawriter_qos);
		}
		dataWriter = (StringDataWriter) publisher.create_datawriter(topic,
				datawriter_qos, null, StatusKind.STATUS_MASK_NONE);
		if (dataWriter == null) {
			logger.error("Unable to create data writer\n");
		}
	}

	@Override
	public void sendMessageForThroughput(byte[] message) {
		String toWrite = new String(message);
		int len = toWrite.length();
		int exceptionCnt =0;
		long messageCounter = 0;
		setStartTime(new Date());
		while (canContinue) {
			try {
				for (int l = 0; l < 200; l++) {
					try{
					dataWriter.write(toWrite+l, InstanceHandle_t.HANDLE_NIL);
					messageCounter++;
					addToStatisticsPool(len);
					}catch (RETCODE_ERROR e) {
						exceptionCnt++;
					}
				}
				if (getThinkTimeInMillis() > 0) {
					try {
						Thread.sleep(getThinkTimeInMillis());
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if (asyncFlag) {
					/*
					 * Once all gather send buffers are used up, we cannot
					 * coalesce any further samples, so send the message now.
					 */
					// System.out.println("Using Aysnc Mode ");
					if (messageCounter % samples_per_trigger == 0) {
						// System.out.println("Triggering Flow ");
						flow_controller.trigger_flow();

					}
				}
			}  catch (Exception ex) {
				logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = "
						+ ex);
				markTaskComplete();
				ex.printStackTrace();
				// break;
			} finally {
				if (asyncFlag) {
					flow_controller.trigger_flow();
					dataWriter.wait_for_asynchronous_publishing(threeSecond);
				}
				markTaskComplete();
			}
		}
		
		setEndTime(new Date());
		System.out.println("Sleeping for 8 seconds"); 
		System.out.println("Exceptions "+exceptionCnt);
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void sendMessageForLatency(byte[] message) {
		String strMessage = new String(message);
		int msgSize = strMessage.length();
		int prefixSize = getMessagePrefix().length();
		int sizeToXtract = msgSize - prefixSize + 3;
		strMessage = strMessage.substring(0, sizeToXtract);
		setStartTime(new Date());
		int i = 0;
		while (canContinue) {
			try {
				i++;
				String msgId = getMessagePrefix() + "-" + i + ":";
				String msgToSend = msgId + strMessage;
				dataWriter.write(msgToSend, InstanceHandle_t.HANDLE_NIL);
				addToStatisticsPool(msgId, new Date(), 1, msgToSend.length());
				if (getThinkTimeInMillis() > 0)
					Thread.sleep(getThinkTimeInMillis());
			} catch (RETCODE_ERROR e) {
				// This exception can be thrown from DDS write operation
				e.printStackTrace();
				break;
			} catch (Exception ex) {
				logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = "
						+ ex);
				taskStatus = 1;
				ex.printStackTrace();
				break;
			} finally {
				markTaskComplete(); // IMPORTANT !!
				setEndTime(new Date()); // IMPORTANT !!
				logger.info("sendMessageForThroughput completed.");
			}
		}
	}

	@Override
	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return true;
	}

}
