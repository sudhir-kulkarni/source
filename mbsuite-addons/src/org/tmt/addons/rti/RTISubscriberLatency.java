package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.SubscriberBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataReader;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;

public class RTISubscriberLatency extends SubscriberBase {

	private RTISubscriber_INNER rtiSub;
	private static boolean shutdown_flag = false;
	private StringDataWriter dataWriter;
	private DomainParticipant participant;
	public static Logger logger = Logger.getLogger(RTISubscriberLatency.class);
	private String serverTopicToSend = "Topic_1";// "latencytest_recv";
	private String serverTopicToRecv = "Topic_2";// "latencytest_send";
	boolean msgSentFlag = false;
	boolean msgRecvdFlag = false;
	private Topic topic;
	private String message;
	private static long messageCounter =0;

	@Override
	public void init(Map<String, String> attributes) {
		rtiSub = new RTISubscriber_INNER();
		rtiSub.init(attributes);

		initPublisher(attributes);
	}

	private void initPublisher(Map<String, String> attributes) {
		participant = DomainParticipantFactory.get_instance()
				.create_participant(0, // Domain ID = 0
						DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null, // listener
						StatusKind.STATUS_MASK_NONE);

		if (participant == null) {
			logger.debug("Unable to create domain participant");
			return;
		}
		logger.debug(" QOS Settings "
				+ DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT.property.policy_name);
		logger.debug("Topic to send " + serverTopicToSend);
		createTopic(serverTopicToSend);
			createDataWriter();
		logger.debug("Ready to write data.");
	}

	/**
	 * Create a topic from the message generated
	 */
	private void createTopic(String topicName) {
		logger.debug("Creating Topic " + topicName);
		topic = participant.create_topic(topicName,
				StringTypeSupport.get_type_name(),
				DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
				StatusKind.STATUS_MASK_NONE);
		if (topic == null) {
			logger.debug("Unable to create topic.");
			return;
		}
	}

	private void createDataWriter() {
		logger.debug("Creating the Data Writer");
		// Create the data writer using the default publisher
		dataWriter = (StringDataWriter) participant.create_datawriter(topic,
				Publisher.DATAWRITER_QOS_DEFAULT, null, // listener
				StatusKind.STATUS_MASK_NONE);
		if (dataWriter == null) {
			logger.debug("Unable to create data writer\n");
			return;
		}
	}

	@Override
	public void read() {
		// rtiSub.read();
		// String msgReceived = rtiSub.getMessage();
		// logger.debug("Message received " + msgReceived);
		// logger.debug("Resending the message back to publisher ");
		// Resend the message receivd
		// dataWriter.write(msgReceived, InstanceHandle_t.HANDLE_NIL);
		
	}

	@Override
	public void shutdown() {
		System.out.println("Total message received "+messageCounter);
		rtiSub.shutdown();
		if(participant != null){
			participant.delete_contained_entities();
			DomainParticipantFactory.get_instance().delete_participant(
					participant);
						logger.debug("Shutdown of DomainParticipant factory complete.");
			}
		
		logger.debug("Task Complete");

	}

	class RTISubscriber_INNER extends DataReaderAdapter {
		private DomainParticipant receiverParticipant;
		private Topic topic;
		private StringDataReader dataReader;

		public void init(Map<String, String> attributes) {
			receiverParticipant = DomainParticipantFactory.get_instance()
					.create_participant(
							0, // Domain ID = 0
							DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
							null, // listener
							StatusKind.STATUS_MASK_NONE);
			if (receiverParticipant == null) {
				logger.debug("Unable to create domain participant");
				return;
			}
			logger.debug("Topic to receive " + serverTopicToRecv);
			createTopic(serverTopicToRecv);
			logger.debug("Creating the Data Reader ");
			createDataReader();
			logger.debug("Ready to read data.");
		}

		public void read() {

			// for (;;) {
			// try {
			// Thread.sleep(2000);
			// if(shutdown_flag) break;
			// } catch (InterruptedException e) {
			// // Nothing to do...
			// }
			// }

		}

		public void shutdown() {
			logger.debug("Shutting down...");
			shutdown_flag = true;
			if(receiverParticipant != null){
			receiverParticipant.delete_contained_entities();
			DomainParticipantFactory.get_instance().delete_participant(
					receiverParticipant);
						logger.debug("Shutdown of DomainParticipant factory complete.");
			}
		}

		/**
		 * Create a topic from the message generated
		 */
		private void createTopic(String topicName) {
			logger.debug("Creating Topic " + topicName);
			topic = receiverParticipant.create_topic(topicName,
					StringTypeSupport.get_type_name(),
					DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
					StatusKind.STATUS_MASK_NONE);
			if (topic == null) {
				logger.debug("Unable to create topic.");
				return;
			}

		}

		private void createDataReader() {
			// Create the data reader using the default publisher
			dataReader = (StringDataReader) receiverParticipant
					.create_datareader(topic,
							Subscriber.DATAREADER_QOS_DEFAULT,
							new RTISubscriber_INNER(), // Listener
							StatusKind.DATA_AVAILABLE_STATUS);
			if (dataReader == null) {
				System.err.println("Unable to create DDS Data Reader");
				return;
			}
		}

		/*
		 * This method gets called back by DDS when one or more data samples
		 * have been received.
		 */
		public void on_data_available(DataReader reader) {
			messageCounter++;
			StringDataReader stringReader = (StringDataReader) reader;
			SampleInfo info = new SampleInfo();
			for (;;) {
				try {
					String msgReceived = stringReader.take_next_sample(info);
//					logger.debug("message before check = " + msgReceived);
					if (info.valid_data) {
						// logger.debug(message);
//						Date dateRecvd = new Date();
//						logger.debug("Message Received - " + msgReceived);
//						setMessage(msgReceived);
						dataWriter.write(msgReceived,
								InstanceHandle_t.HANDLE_NIL);
					}
				} catch (RETCODE_NO_DATA noData) {
//					logger.debug("Finished reading all data. No more data to read.");
					break;
				} catch (RETCODE_ERROR e) {
//					logger.error("An error occurred while reading data.", e);
				}
			}
		}

		private void setMessage(String msg) {
			shutdown_flag = true;
			message = msg;
		}

		public String getMessage() {
			return message;
		}
	}

}
