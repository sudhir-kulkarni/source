package org.tmt.addons.sample;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.PublisherBase;

public class SamplePublisher extends PublisherBase {
	private static final Logger logger = Logger
			.getLogger(SamplePublisher.class);
	private String topic;

	@Override
	public void init(Map<String, String> attributes) {
		logger.info("Initializing Sample Publisher...");
		super.init(attributes);
		topic = attributes.get("topic");
		initializeMessagePlatform();
		logger.info("Sample Publisher initialization complete...");
	}

	private void initializeMessagePlatform() {
		// Write code to instantiate or initialize any objects.
		// Generally these will be activities you do only once. Like opening a
		// connection to the
		// messaging platform etc
		logger.info("Platform specific init done");
	}
	@Override
	public boolean isReadyToRun() {
		return true;
	}

	
	@Override
	public void sendMessageForThroughput(byte[] message) {
		logger.info("sendMessageForThroughput started.");
		setStartTime(new Date());
		try {
			while (canContinue) {
				// send messages till the suite does not tell you to shutdown
				sendMessage(topic, message);

				// add data to statistics pool. This will count the number of
				// messages and the total bytes sent.
				addToStatisticsPool(message.length);

				// In case you need to consider think time consider it like
				// below
				if (getThinkTimeInMillis() > 0)
					Thread.sleep(getThinkTimeInMillis());
			}
		} catch (Exception e) {
			logger.error("Exception encountered in Publisher program");
			e.printStackTrace();
		} finally {
			markTaskComplete(); // IMPORTANT !!
			setEndTime(new Date()); // IMPORTANT !!
			logger.info("sendMessageForThroughput completed.");
		}
	}

	private void sendMessage(String topic2, byte[] message) {
		logger.info("message sent");
		// write code to send the message as per the semantics
		// of the messaging api.
	}

	@Override
	public void sendMessageForLatency(byte[] message) {
		setStartTime(new Date());
		String mstr = new String(message);
		try {
			int i = 0;
			while (canContinue) {
				// send messages till the suite does not tell you to shutdown

				// Generate unique identifier for each message
				i++;
				String messageIdentifier = getMessagePrefix() + i;
				// prepend the identifier to the message. Get the exact number
				// of bytes that were requested in the config.
				String messageToSend = (messageIdentifier + ":" + mstr).substring(0,
						getMessageLength());
				

				Date sentDate = new Date();
				// Send the message. Now the message contains a unique
				// identifier which is used for calculating latency.
				sendMessage(topic, messageToSend.getBytes());

				// add data to statistics pool. This will count the number of
				// messages and the total bytes sent.
				addToStatisticsPool(messageIdentifier, sentDate, 1,
						messageToSend.length());

				// In case you need to consider think time, consider it like
				// below
				if (getThinkTimeInMillis() > 0)
					Thread.sleep(getThinkTimeInMillis());
			}
		} catch (Exception e) {
			logger.error("Exception encountered in Publisher program");
			e.printStackTrace();
		} finally {
			markTaskComplete(); // IMPORTANT !!
			setEndTime(new Date()); // IMPORTANT !!
			logger.info("sendMessageForThroughput completed.");
		}

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}


}
