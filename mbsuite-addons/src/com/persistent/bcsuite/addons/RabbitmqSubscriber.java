package com.persistent.bcsuite.addons;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;
import com.persistent.bcsuite.base.SubscriberBase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

//import com.rabbitmq.client.test.functional.RequeueOnChannelClose;

public class RabbitmqSubscriber extends SubscriberBase {
	private static final Logger logger = Logger
			.getLogger(RabbitmqSubscriber.class);
	boolean isInited;
	private String exchangeType;
	private String serverHostIP;
	private String serverHostPort;
	private ConnectionFactory factory;
	private Connection connection;
	private String serverTopic;
	private Channel channel;
	private String qName = null;
	private QueueingConsumer consumer;

	@Override
	public void init(Map<String, String> attributes) {
		logger.info("RabbitmqSubscriber initializing");
		serverHostIP = attributes.get("host-ip");
		serverHostPort = attributes.get("host-port");
		serverTopic = attributes.get("topic");
		exchangeType = attributes.get("ex-type");
		try {
			factory = new ConnectionFactory();
			factory.setHost(serverHostIP);
			factory.setPort(Integer.parseInt(serverHostPort));
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(serverTopic, exchangeType);
			consumer = new QueueingConsumer(channel);
			qName = channel.queueDeclare().getQueue();
			System.out.println("queuename--" + qName);
			channel.queueBind(qName, serverTopic, "");
		} catch (Exception ex) {
			System.err.println("init thread caught exception: " + ex);
			ex.printStackTrace();
			System.exit(1);
		}
		logger.info("RabbitmqSubscriber inited");
	}

	@Override
	public void read() {
		// Send the message
		// Return back the messageId of the message
		// Dont do any other logic in this method.
		// System.out.println("Read called");
		try {
			channel.basicConsume(qName, true, consumer);
			while (true) {
				QueueingConsumer.Delivery delivery = null;
				try {
					delivery = consumer.nextDelivery();
				} catch (ShutdownSignalException sex) {
					System.out.println("Consumer closed");
					break;
				}
				String message = new String(delivery.getBody());
				logger.debug("Message recieved = " + message);
				Date recdOn = new Date();
				if (message.indexOf(":") != -1) {
					String msgId = message.substring(0, message.indexOf(":"));
					int messageSize = message.getBytes().length;
					addToStatisticsPool(msgId, messageSize, recdOn);
				} else {
					addToStatisticsPool(null, message.length(), recdOn);
				}
			}
			markTaskComplete();
		} catch (Exception ex) {
			System.err.println("read thread caught exception: " + ex);
			ex.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		try {
			if (channel.isOpen())
				channel.close();
			if (connection.isOpen())
				connection.close();

			System.out.println("Shutting Down SampleSubscriber");
		} catch (ShutdownSignalException se) {
			System.out.println("Closed.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
