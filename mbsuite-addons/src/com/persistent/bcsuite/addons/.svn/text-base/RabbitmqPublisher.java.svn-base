package com.persistent.bcsuite.addons;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;
import com.persistent.bcsuite.base.PublisherBase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitmqPublisher extends PublisherBase {
	private static final Logger logger = Logger
			.getLogger(RabbitmqPublisher.class);
	boolean isInited;
	private String serverHostIP;
	private String serverHostPort;
	private String serverTopic;
	private String exchangeType;
	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel=null;

	public void init(Map<String, String> attributes) {
		logger.info("Initializing Sample Publisher...");
		super.init(attributes);
		serverHostIP = attributes.get("host-ip");
		serverHostPort = attributes.get("host-port");
		serverTopic = attributes.get("topic");
		exchangeType = attributes.get("ex-type");
		initializeMessagePlatform();
		logger.info("Sample Publisher initialization complete...");
	}

	private void initializeMessagePlatform() {
		// TODO Auto-generated method stub
		try {
			factory = new ConnectionFactory();
			factory.setHost(serverHostIP);
			factory.setPort(Integer.parseInt(serverHostPort));
			connection = factory.newConnection();

			if (channel == null)
				channel = connection.createChannel();

			channel.exchangeDeclare(serverTopic, exchangeType);
		} catch (Exception ex) {
			System.err.println("Init Exception " + ex);
			ex.printStackTrace();
			System.exit(1);
		}
		logger.info("RabbitmqPublisher inited");

	}

	@Override
	public boolean isReadyToRun() {
		return true;
	}


	@Override
	public void sendMessageForThroughput(byte[] message) {
		// TODO Auto-generated method stub

		logger.info("sendMessageForThroughput started.");
		setStartTime(new Date());
		try {
			while (canContinue) {
				// send messages till the suite does not tell you to shutdown
				sendMessage(serverTopic, message);
				// logger.info("message sent");

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

	private void sendMessage(String serverTopic, byte[] message) {
		// TODO Auto-generated method stub
		try {
			channel.basicPublish(serverTopic, "", null, message);
			// logger.info("message sent");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void sendMessageForLatency(byte[] message) {
		// TODO Auto-generated method stub
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
				sendMessage(serverTopic, messageToSend.getBytes());

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
		try {
			if (channel.isOpen())
				channel.close();
			if (connection.isOpen())
				connection.close();
			System.out.println("Shutting Down SampleSubscriber");
		} catch (ShutdownSignalException se) {
			System.out.println("Closed.");
			// System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
