package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.tmt.addons.rti.RTISubscriber.RTIListener;

import com.persistent.bcsuite.base.PublisherBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataReader;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;

public class RTIPublisherLatency extends PublisherBase {

	private RTISubscriberLatency_INNER rtiSub;
	private static boolean shutdown_flag = false;
	private DomainParticipant participant;
	private DomainParticipant receiverParticipant;
	private Topic topic;
	private Topic receiveTopic;
	private StringDataReader dataReader;
	private String topicName;
	private StringDataWriter dataWriter;
	// private String topicName;
	public static Logger logger = Logger.getLogger(RTIPublisherLatency.class);
	private String serverTopicToSend = "Topic_2";// "latencytest_send";
	private String serverTopicToRecv = "Topic_1";// "latencytest_recv";
	boolean msgRecvdFlag = false;
	private String message;
	private static long messageCounter = 0;
	private static long messagesSent = 0;
	private static long messagesRcvd = 0;

	@Override
	public void init(Map<String, String> attributes) {
		participant = DomainParticipantFactory.get_instance()
				.create_participant(0, // Domain ID = 0
						DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null, // listener
						StatusKind.STATUS_MASK_NONE);

		if (participant == null) {
			logger.error("Unable to create domain participant");
			return;
		}

		createTopic(serverTopicToSend);
		if (dataWriter == null)
			createDataWriter();
		logger.debug("Ready to write data.");
		initSubscriber(attributes);
	}

	private void initSubscriber(Map<String, String> attributes) {
		rtiSub = new RTISubscriberLatency_INNER();
//		rtiSub.init(attributes);
		 receiverParticipant = DomainParticipantFactory.get_instance()
			 .create_participant(
			 0, // Domain ID = 0
			 DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
			 null, // listener
			 StatusKind.STATUS_MASK_NONE);
			 if (receiverParticipant == null) {
			 logger.error("Unable to create domain participant");
			 return;
			 }
			 createTopicForReceiver(serverTopicToRecv);
			 logger.error("Creating the Data Reader ");
			 createDataReader();
			 logger.info("Ready to read data.");

	}

	@Override
	public boolean isReadyToRun() {
		boolean isReadyToRun = false;
		PublicationMatchedStatus pubMatch = new PublicationMatchedStatus();
		dataWriter.get_publication_matched_status(pubMatch);
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

	}

	@Override
	public void sendMessageForLatency(byte[] message) {
		System.out.println("Inside Send Message ");
		// Send message on topic T1
		setStartTime(new Date());
		int matchCnt = 0;
		int nonMatchCnt = 0;
		int msgNum = 1;
		while (canContinue()) {
			try {
				String msgToSend = new String(message);
				// long sentOn = System.nanoTime();
				// System.out.println("Sending Message ");
				// logger.debug("Message Id : " + msgToSend.substring(0, 10));
				messagesSent++;
				dataWriter.write(String.valueOf(System.nanoTime()) + ":"
						+ msgToSend, InstanceHandle_t.HANDLE_NIL);
				// logger.debug("Message Sent" +msgToSend);
				// logger.debug("Waiting for message to be received ");
				// addToStatisticsPool(msgToSend, new Date(), 1,
				// msgToSend.length());
				// Read on topic T2
				// rtiSub.read();
				String msgReceived = rtiSub.getMessage();
				if (msgRecvdFlag) {
					// System.out.println("Message received back by Publisher");
				}
				// long recdOn = System.nanoTime();
				// logger.error("Message received " + msgReceived);
				// // Compare the msg received with the one sent
				if (msgToSend.equals(msgReceived)) {
					// logger.debug("Messages matched ");
					matchCnt++;
				} else {
					// logger.debug("Messages not matching ");
					nonMatchCnt++;
				}
				// float f = (recdOn - sentOn) / 2;
				// f = (float) (Math.round(f * 100.0) / 100.0);
				// logger.debug("f = " + f);
				// addLatencyStats(msgNum++, f);
				// addToStatisticsPool(null, null, 1, message.length);
				TimeUnit.MICROSECONDS.sleep(1);
//				TimeUnit.NANOSECONDS.sleep(900);
//				if(messagesSent % 100 == 0){
//					TimeUnit.MICROSECONDS.sleep(10);
//				}
				if (getThinkTimeInMillis() > 0)
					Thread.sleep(getThinkTimeInMillis());
			} catch (RETCODE_ERROR e) {
				// This exception can be thrown from DDS write operation
				e.printStackTrace();
			} catch (Exception ex) {
				logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = "
						+ ex);

				ex.printStackTrace();

			}
		}
		setEndTime(new Date());
		System.out.println("Publisher Sleeping for 15 seconds");
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Messages Matched Count : " + matchCnt);
		logger.info("Messages Unmatched Count : " + nonMatchCnt);
		markTaskComplete();
	}

	@Override
	public void cleanup() {
		logger.info("Messages Sent : " + messagesSent);
		logger.info("Messages Received : " + messagesRcvd);
//		rtiSub.shutdown();
		logger.info("Exiting...");
		participant.delete_contained_entities();
		DomainParticipantFactory.get_instance().delete_participant(participant);
		receiverParticipant.delete_contained_entities();
		 DomainParticipantFactory.get_instance().delete_participant(
				 receiverParticipant);
		 logger.info("Shutdown of DomainParticipant factory complete.");
	}

	/**
	 * Create a topic from the message generated
	 */
	private Topic createTopic(DomainParticipant participnt, Topic topic_to_create, String topicName) {
		logger.info("Creating Topic " + topicName);
		topic_to_create = participnt.create_topic(topicName,
				StringTypeSupport.get_type_name(),
				DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
				StatusKind.STATUS_MASK_NONE);
		if (topic == null) {
			logger.error("Unable to create topic.");
			return null;
		}
		
		return topic_to_create;

	}
	
	/**
	 * Create a topic from the message generated
	 */
	private void createTopic(String topicName) {
		logger.info("Creating Topic " + topicName);
		topic = participant.create_topic(topicName,
				StringTypeSupport.get_type_name(),
				DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
				StatusKind.STATUS_MASK_NONE);
		if (topic == null) {
			logger.error("Unable to create topic.");
			return ;
		}
		
		

	}
	
	// /**
	// * Create a topic from the message generated
	// */
	 private void createTopicForReceiver(String topicName) {
	 logger.info("Creating Topic " + topicName);
	 receiveTopic = receiverParticipant.create_topic(topicName,
	 StringTypeSupport.get_type_name(),
	 DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
	 StatusKind.STATUS_MASK_NONE);
	 if (receiveTopic == null) {
	 logger.error("Unable to create topic.");
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
			logger.error("Unable to create data writer\n");
			return;
		}
	}
	
	private void createDataReader() {

		// Create the data reader using the default publisher
		dataReader = (StringDataReader) receiverParticipant.create_datareader(
				receiveTopic, Subscriber.DATAREADER_QOS_DEFAULT,
				new RTISubscriberLatency_INNER(), // Listener
				StatusKind.DATA_AVAILABLE_STATUS);
		if (dataReader == null) {
			logger.error("Unable to create DDS Data Reader");
			return;
		}
	}

	class RTISubscriberLatency_INNER extends DataReaderAdapter {

		

		// public void shutdown() {
		// logger.debug("Shutting down...");
		// shutdown_flag = true;
		// 
		// }

		// /**
		// * Create a topic from the message generated
		// */
		// private void createTopic(String topicName) {
		// logger.info("Creating Topic " + topicName);
		// receiveTopic = receiverParticipant.create_topic(topicName,
		// StringTypeSupport.get_type_name(),
		// DomainParticipant.TOPIC_QOS_DEFAULT, null, // listener
		// StatusKind.STATUS_MASK_NONE);
		// if (receiveTopic == null) {
		// logger.error("Unable to create topic.");
		// return;
		// }
		//
		// }

		// 

		/*
		 * This method gets called back by DDS when one or more data samples
		 * have been received.
		 */
		public void on_data_available(DataReader reader) {
			StringDataReader stringReader = (StringDataReader) reader;
			SampleInfo info = new SampleInfo();
			 for (;;) {
			try {
				messageCounter++;
				String message = stringReader.take_next_sample(info);
				long recdTimeNano = System.nanoTime();
				messagesRcvd++;
				if (info.valid_data) {
					Date dateRecvd = new Date();
					setMessage(message);
					if (messageCounter > 1000) {
						messageCounter = 0;
						int idx = message.indexOf(":");
						if (idx != -1) {
							String sentTime = message.substring(0, idx);
							long sentTimeNano = Long.parseLong(sentTime);
							int latency = (int) (recdTimeNano - sentTimeNano);
							System.out
									.println("Latency for Message " + latency);
							// addToStatisticsPool(null,new
							// Date(),message.length(),latency);
						}
					}
					shutdown_flag = true;
					msgRecvdFlag = true;
				}
			} catch (RETCODE_NO_DATA noData) {
				// No more data to read
				break;

			} catch (RETCODE_ERROR e) {
				logger.error("An error occurred while reading data.", e);
			}
			 }
		}

		private void setMessage(String msg) {
			message = msg;
		}

		public String getMessage() {
			return message;
		}
	}

}
