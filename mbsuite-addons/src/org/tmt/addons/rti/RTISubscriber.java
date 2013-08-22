package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.SubscriberBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.DataReaderResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.HistoryQosPolicyKind;
import com.rti.dds.infrastructure.RETCODE_ALREADY_DELETED;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.infrastructure.TransportBuiltinKind;
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
import com.rti.dds.type.builtin.StringDataReader;
import com.rti.dds.type.builtin.StringTypeSupport;
import com.rti.ndds.transport.TransportSupport;
import com.rti.ndds.transport.UDPv4Transport;

public class RTISubscriber extends SubscriberBase {

	private static Logger logger = Logger.getLogger(RTISubscriber.class);
	static final int MESSAGE_SIZE_MAX = 8192; // 65535
	static final int UDP_SIZE_MAX = 65536;
	static final int MAX_SAMPLES = 1024;
//	static final int MAX_EVENT_COUNT = (1024 * 16);
	private static DomainParticipantFactory factory;
	private static DomainParticipant participant;
	private long messageCounter = 0;
	private Topic topic;
	private Subscriber subscriber;
	private StringDataReader dataReader;
	private String topicName;
	private boolean isReliable = false;
	private final Duration_t threeSecond = new Duration_t(3, 0);
	private static int counter =0 ;

	@Override
	public void init(Map<String, String> attributes) {
		// rtiListener.init(attributes);
		String strReliable = attributes.get("isReliable");
		if (strReliable != null && !strReliable.isEmpty()) {
			isReliable = Boolean.parseBoolean(strReliable);
			System.out.println("Setting Reliability " +isReliable);
		}
		if (factory == null) {
			initFactory();
		}
		if (participant == null) {
			initDomainParticipant();
		}

		topicName = attributes.get("topic");
		
		// Create Topic
		createTopic(topicName);
		initSubscriber();
		createDataReader();
		System.out.println("Ready to read data.");
		incrementCounter();

	}

	private void initSubscriber() {
		SubscriberQos subscriber_qos = new SubscriberQos();
		participant.get_default_subscriber_qos(subscriber_qos);
		subscriber = participant.create_subscriber(subscriber_qos,
				null, StatusKind.STATUS_MASK_NONE);
		
	}

	private void initDomainParticipant() {
		DomainParticipantQos participant_qos = new DomainParticipantQos();
		factory.get_default_participant_qos(participant_qos);
		configure_participant_qos(participant_qos, factory);

		participant = factory.create_participant(0, // Domain ID = 0
				participant_qos, null, // listener
				StatusKind.STATUS_MASK_NONE);
		UDPv4Transport.Property_t udpv4TransportProperty = new UDPv4Transport.Property_t();
		configure_participant_transport(udpv4TransportProperty, participant);
//		StringTypeSupport.register_type(participant,
//				StringTypeSupport.get_type_name());
		if (participant == null) {
			System.out.println("Unable to create domain participant");
			return;
		}
		participant.enable();
	}

	private void initFactory() {
		factory = DomainParticipantFactory.get_instance();
		DomainParticipantFactoryQos factory_qos = new DomainParticipantFactoryQos();
		configure_factory_qos(factory_qos, factory);

	}

	public void configure_factory_qos(DomainParticipantFactoryQos factory_qos,
			DomainParticipantFactory factory) {
		// We need to disable participants so that we can
		// plug in a new/modified transport
		factory.get_qos(factory_qos);
		factory_qos.entity_factory.autoenable_created_entities = false;
		factory.set_qos(factory_qos);
	}

	public void configure_data_reader_qos(DataReaderQos data_reader_qos,
			Subscriber subscriber) {
		subscriber.get_default_datareader_qos(data_reader_qos);
		// use these hard coded value until you use key
//		data_reader_qos.resource_limits.max_instances = 1;
//		data_reader_qos.resource_limits.initial_instances = data_reader_qos.resource_limits.max_instances;
//
//		data_reader_qos.resource_limits.max_samples_per_instance = 1;
//		data_reader_qos.reader_resource_limits.max_samples_per_remote_writer = data_reader_qos.resource_limits.LENGTH_UNLIMITED;//max_samples_per_instance;
//		data_reader_qos.resource_limits.max_samples = data_reader_qos.resource_limits.LENGTH_UNLIMITED;
//		data_reader_qos.resource_limits.initial_samples = data_reader_qos.resource_limits.max_samples_per_instance;
//		data_reader_qos.reader_resource_limits.max_samples_per_read = 60000;
//		  qos.resource_limits.max_samples
//New Changes{
			data_reader_qos.resource_limits.max_samples = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
			data_reader_qos.resource_limits.max_instances = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
			data_reader_qos.resource_limits.max_samples_per_instance = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
			data_reader_qos.reader_resource_limits.max_samples_per_remote_writer = ResourceLimitsQosPolicy.LENGTH_UNLIMITED;
			data_reader_qos.reader_resource_limits.max_samples_per_read = 60000;
			data_reader_qos.reader_resource_limits.max_total_instances = DataReaderResourceLimitsQosPolicy.AUTO_MAX_TOTAL_INSTANCES;
		//New Changes}
		
			if (isReliable) {
				data_reader_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
			} else {
				data_reader_qos.reliability.kind = ReliabilityQosPolicyKind.BEST_EFFORT_RELIABILITY_QOS;
			}
		
		// Unless specified, we will use best effort for this test so...
		if (isReliable) {
//			DataReaderQos_setReliableBursty(data_reader_qos);
//			data_reader_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
		}//else{
			//data_reader_qos.reliability.kind = ReliabilityQosPolicyKind.BEST_EFFORT_RELIABILITY_QOS;
		//}
	}

	public void configure_participant_qos(DomainParticipantQos participant_qos,
			DomainParticipantFactory factory) {

		// Configure the Participant Quality of Service
		factory.get_default_participant_qos(participant_qos);
		// participant_qos.wire_protocol.participant_id =
		// args.participantId;
		// Ensure that when we receive data it is processed quickly
		participant_qos.event.thread.priority = Thread.MAX_PRIORITY;
		participant_qos.receiver_pool.thread.priority = Thread.MAX_PRIORITY - 1;
		// So we can receive large packets.
		participant_qos.receiver_pool.buffer_size = MESSAGE_SIZE_MAX
				* (MAX_SAMPLES * 2);
		participant_qos.transport_builtin.mask = 0;// clear all xport first
		// if (args.transportToUse() == TransportBuiltinKind.UDPv4) {
		participant_qos.transport_builtin.mask |= TransportBuiltinKind.UDPv4;
	}

	public void configure_participant_transport(
			UDPv4Transport.Property_t udpv4TransportProperty,
			DomainParticipant participant) {
		// Configure built in IPv4 transport to handle large messages
		TransportSupport.get_builtin_transport_property(participant,
				udpv4TransportProperty);
		udpv4TransportProperty.message_size_max = UDP_SIZE_MAX;
		udpv4TransportProperty.send_socket_buffer_size = udpv4TransportProperty.message_size_max;
		udpv4TransportProperty.recv_socket_buffer_size = udpv4TransportProperty.message_size_max * 2;
		  
		// // ONLY one interface permitted for test
//		 String locator = "10.88.203.40";
//		 if(locator != null && !locator.equals("")) {
//		 udpv4TransportProperty.allow_interfaces_list.clear();
//		 udpv4TransportProperty.allow_interfaces_list.add(locator);
//		 }
		TransportSupport.set_builtin_transport_property(participant,
				udpv4TransportProperty);
	}

	private void DataReaderQos_setReliableBursty(DataReaderQos qos) {
		DataReaderQos_setReliableBursty(qos, 1);
	}

	private void DataReaderQos_setReliableBursty(DataReaderQos qos,
			int remoteWriterCountMax) {
		qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
		qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

		// reader queue can be constant regardless of rate
		int unresolvedSamplePerRemoteWriterMax = 100;

		qos.resource_limits.max_samples = remoteWriterCountMax
				* unresolvedSamplePerRemoteWriterMax;
		qos.resource_limits.initial_samples = qos.resource_limits.max_samples;

		qos.reader_resource_limits.max_samples_per_remote_writer = qos.resource_limits.initial_samples;
		qos.resource_limits.max_samples_per_instance = qos.resource_limits.initial_samples;

		// the writer probably has more for the reader; ask right away
		qos.protocol.rtps_reliable_reader.min_heartbeat_response_delay.sec = 0;
		qos.protocol.rtps_reliable_reader.min_heartbeat_response_delay.nanosec = 0;
		qos.protocol.rtps_reliable_reader.max_heartbeat_response_delay.sec = 0;
		qos.protocol.rtps_reliable_reader.max_heartbeat_response_delay.nanosec = 0;
	}

	@Override
	public void read() {
		// rtiListener.read();

	}

	@Override
	public void shutdown() {
		// rtiListener.shutdown();
		decrementCounter();
		markTaskComplete();
		System.out.println("Messages received " + messageCounter);
		if(counter == 0){
		System.out.println("All threads finished processing. Shutting down...");
		if (participant != null) {
			try{
			participant.delete_contained_entities();
			DomainParticipantFactory.get_instance().delete_participant(
					participant);
			System.out
					.println("Shutdown of DomainParticipant factory complete.");
			}catch(RETCODE_ALREADY_DELETED ex){
				logger.debug("Participant Already Closed.");
			}
		}
		}
		System.out.println("Task Complete");

	}
	
	private static synchronized void incrementCounter(){
		counter++;
	}
	
	private static synchronized void decrementCounter(){
		counter--;
	}
	

	/**
	 * Create a topic from the message generated
	 */
	private void createTopic(String topicName) {
		
		TopicQos data_topic_qos = new TopicQos();
		participant.get_default_topic_qos(data_topic_qos);
		Topic tempTopic =null;
		
		try{
		 tempTopic = participant.create_topic(topicName,
				StringTypeSupport.get_type_name(), data_topic_qos
				/* DomainParticipant.TOPIC_QOS_DEFAULT */, null, // listener
				StatusKind.STATUS_MASK_NONE);
		}catch (RETCODE_ERROR error) {
			logger.debug("Cannot Create topic. Topic already exists ");
		}
		if (tempTopic == null) {
			System.out
					.println("Unable to create topic. Topic may already exist.");
			logger.debug("Topic already exists. Getting handle to topic");
			topic = participant.find_topic(topicName, threeSecond);
			logger.debug("Discovered Topic " + topic.get_name());
			System.out.println("Discovered Topic " + topic.get_name());
		} else {
			topic = tempTopic;
			System.out.println("Creating Topic " + topicName);
		}

	}

	private void createDataReader() {
		DataReaderQos data_reader_qos = new DataReaderQos();
		configure_data_reader_qos(data_reader_qos, subscriber);
		RTIListener listener = new RTIListener();
		// Create the DataReader
		// createDataReader();
		dataReader = (StringDataReader) subscriber.create_datareader(topic,
				data_reader_qos, listener, StatusKind.STATUS_MASK_ALL);
//		StringDataReader reader2 = (StringDataReader) subscriber.create_datareader(topic,
//				data_reader_qos,listener , StatusKind.STATUS_MASK_ALL);
//		StringDataReader reader3 = (StringDataReader) subscriber.create_datareader(topic,
//				data_reader_qos,listener , StatusKind.STATUS_MASK_ALL);
//		StringDataReader reader4 = (StringDataReader) subscriber.create_datareader(topic,
//				data_reader_qos,listener , StatusKind.STATUS_MASK_ALL);
//		StringDataReader reader5 = (StringDataReader) subscriber.create_datareader(topic,
//				data_reader_qos,listener , StatusKind.STATUS_MASK_ALL);
		if (dataReader == null) {
			System.err.println("Unable to create DDS Data Reader");
			return;
		}
	}

	class RTIListener extends DataReaderAdapter {
		boolean isNotPrint=true;
		/*
		 * This method gets called back by DDS when one or more data samples
		 * have been received.
		 */
		public void on_data_available1(DataReader reader) {
			StringDataReader stringReader = (StringDataReader) reader;
			SampleInfo info = new SampleInfo();
			for (;;) {
				try {
					String message = stringReader.take_next_sample(info);
					// System.out.println("message before check = " + message);
					if (info.valid_data) {
						// System.out.println(message);
						Date dateRecvd = new Date();
						if (!message.isEmpty() && message.length() > 10) {
							// String msgId = message.substring(0, 5);
							messageCounter++;
							// addToStatisticsPool(msgId, msgSize, dateRecvd);
							addToStatisticsPool(null,
									message.getBytes().length, dateRecvd);
						}
					}
				} catch (RETCODE_NO_DATA noData) {
					// No more data to read
					break;
				} catch (RETCODE_ERROR e) {
					// An error occurred
					e.printStackTrace();
				}
			}
		}

		public void on_data_available(DataReader reader) {
			
			dataReader = (StringDataReader) reader;
			SampleInfoSeq infoSeq = new SampleInfoSeq(); // sequence of
			StringSeq dataSeq = new StringSeq();
			String message;
			try {
				dataReader.take(dataSeq, infoSeq,
						ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
						SampleStateKind.NOT_READ_SAMPLE_STATE,
						ViewStateKind.ANY_VIEW_STATE,
						InstanceStateKind.ALIVE_INSTANCE_STATE);
//				System.out.println("received message " + dataSeq.size());
				for (int i = 0; i < dataSeq.size(); ++i) {
					if (((SampleInfo) infoSeq.get(i)).valid_data) {
						message = (String) dataSeq.get(i);
						messageCounter++;
						// Reset sequence number
						// number
						addToStatisticsPool(null, message.getBytes().length,
								new Date());
						if(isNotPrint){
						System.out.println("Message "+message);
							isNotPrint =false;
						}
					}
				}
				dataReader.return_loan(dataSeq, infoSeq);
			} catch (RETCODE_NO_DATA noData) {
				// No data to process
			}
		}
	}
}
