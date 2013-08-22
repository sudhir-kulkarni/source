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
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.RETCODE_ALREADY_DELETED;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
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
import com.rti.dds.type.builtin.StringDataReader;

public class RTISimpleSub extends SubscriberBase {

	private static Logger logger = Logger.getLogger(RTISubscriber.class);
	private static DomainParticipantFactory factory;
	private static DomainParticipant participant;
	private long messageCounter = 0;
	private Topic topic;
	private Subscriber subscriber;
	// private StringDataReader dataReader;
	private ThroughputDataReader dataReader;
	private String topicName;
	private boolean isReliable = false;
	private final Duration_t threeSecond = new Duration_t(3, 0);
	private static int counter = 0;
	private RTIListener listener = new RTIListener();

	@Override
	public void init(Map<String, String> attributes) {
		// rtiListener.init(attributes);
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

		topicName = attributes.get("topic");
		System.out.println("Topic name " +topicName);
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
		subscriber = participant.create_subscriber(subscriber_qos, null,
				StatusKind.STATUS_MASK_NONE);

	}

	private void initDomainParticipant() {
		DomainParticipantQos participant_qos = new DomainParticipantQos();
		factory.get_default_participant_qos(participant_qos);

		participant = factory.create_participant(0, // Domain ID = 0
				participant_qos, null, // listener
				StatusKind.STATUS_MASK_NONE);
		if (participant == null) {
			System.out.println("Unable to create domain participant");
			return;
		}
		ThroughputTypeSupport.register_type(participant,
				ThroughputTypeSupport.get_type_name());
	}

	private void initFactory() {
		factory = DomainParticipantFactory.get_instance();
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
		if (counter == 0) {
			System.out
					.println("All threads finished processing. Shutting down...");
			if (participant != null) {
				try {
					participant.delete_contained_entities();
					DomainParticipantFactory.get_instance().delete_participant(
							participant);
					System.out
							.println("Shutdown of DomainParticipant factory complete.");
				} catch (RETCODE_ALREADY_DELETED ex) {
					logger.debug("Participant Already Closed.");
				}
			}
		}
		System.out.println("Task Complete");

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

		TopicQos data_topic_qos = new TopicQos();
		participant.get_default_topic_qos(data_topic_qos);
		Topic tempTopic = null;

		try {
			tempTopic = participant.create_topic(topicName,
					ThroughputTypeSupport.get_type_name(), data_topic_qos,
					null, // listener
					StatusKind.STATUS_MASK_NONE);
		} catch (RETCODE_ERROR error) {
			logger.debug("Cannot Create topic. Topic may already exists ");
			error.printStackTrace();
		}
		if (tempTopic == null) {
			// System.out
			// .println("Unable to create topic. Topic may already exist.");
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
		subscriber.get_default_datareader_qos(data_reader_qos);
		if (isReliable) {
			data_reader_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
		}
		// Create the DataReader
		dataReader = (ThroughputDataReader) subscriber.create_datareader(topic,
				data_reader_qos, listener, StatusKind.STATUS_MASK_ALL);
		if (dataReader == null) {
			System.err.println("Unable to create DDS Data Reader");
			return;
		}
	}

	class RTIListener extends DataReaderAdapter {
		boolean isNotPrint = true;

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
			messageCounter++;
			ThroughputDataReader dataReader = (ThroughputDataReader) reader;
			SampleInfoSeq infoSeq = new SampleInfoSeq(); // sequence of
			ThroughputSeq dataSeq = new ThroughputSeq();
			byte[] message;
			try {
				dataReader.take(dataSeq, infoSeq,
						ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
						SampleStateKind.NOT_READ_SAMPLE_STATE,
						ViewStateKind.ANY_VIEW_STATE,
						InstanceStateKind.ALIVE_INSTANCE_STATE);
				// System.out.println("received message " + dataSeq.size());
				for (int i = 0; i < dataSeq.size(); ++i) {
					if (((SampleInfo) infoSeq.get(i)).valid_data) {
						message = (byte[]) ((Throughput) dataSeq.get(i)).data
								.getPrimitiveArray();
						// ((Throughput)dataSeq.get(i)).data.toArrayByte(message);
						String strMsg = new String(message);
						addToStatisticsPool(null, strMsg.trim().length(), new Date());
						if (isNotPrint) {
							System.out.println("Message " + message);
							isNotPrint = false;
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
