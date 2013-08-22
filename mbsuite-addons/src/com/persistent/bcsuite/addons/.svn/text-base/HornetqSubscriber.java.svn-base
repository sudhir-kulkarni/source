package com.persistent.bcsuite.addons;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import com.persistent.bcsuite.base.SubscriberBase;

public class HornetqSubscriber extends SubscriberBase {
	private static final Logger logger = Logger
			.getLogger(HornetqSubscriber.class.getName());

	private String queueName;
	private String address;
	private String serverHost;
	private String serverPort;
	private String tcpBuffer;
	private String tcpNoDelay;
	private ClientSessionFactory factory;
	private ClientConsumer consumer;
	private ClientSession session = null;
	private ClientMessage msg = null;
	private String preAck;
	private ServerLocator serverLocator;
	private static Random random = new Random();

	@Override
	public void init(Map<String, String> attributes) {
		logger.info("HornetqSubscriber Initializing");

		queueName = attributes.get("queue-name");
		address = attributes.get("address");
		serverHost = attributes.get("host-ip");
		serverPort = attributes.get("host-port");
		tcpBuffer = attributes.get("tcp-buffer");
		tcpNoDelay = attributes.get("tcp-no-delay");

		preAck = attributes.get("pre-ack");
		Map<String, Object> params = new HashMap<String, Object>();

		params.put(TransportConstants.TCP_NODELAY_PROPNAME, tcpNoDelay);
		params.put(TransportConstants.TCP_SENDBUFFER_SIZE_PROPNAME, tcpBuffer);
		params.put(TransportConstants.TCP_RECEIVEBUFFER_SIZE_PROPNAME,
				tcpBuffer);
		params.put(TransportConstants.HOST_PROP_NAME, serverHost);
		params.put(TransportConstants.PORT_PROP_NAME, serverPort);

		serverLocator = HornetQClient
				.createServerLocatorWithoutHA(new TransportConfiguration(
						NettyConnectorFactory.class.getName(), params));
		serverLocator.setPreAcknowledge(Boolean.parseBoolean(preAck));

		try {

			factory = serverLocator.createSessionFactory();
			session = factory.createSession();
			int r = random.nextInt(10000);
			String s = queueName + "_" + r;
			logger.info("Created random queue = " + s);
			//session.createQueue(address, s, false);
			session.createTemporaryQueue(address, s);
			consumer = session.createConsumer(s);

		}

		catch (Exception e) {
			System.out.println("init() Exception");
			e.printStackTrace();
		}
		logger.info("HornetqSubscriber inited");
	}

	@Override
	public void read() {
		// TODO Auto-generated method stub
		// Send the message
		// Return back the messageId of the message
		// Dont do any other logic in this method.
		try {
			session.start();
			logger.info("READY!!!");
			while (!consumer.isClosed()) {
				msg = consumer.receive();
				if (msg == null) {
					continue;
				}
				String message = msg.getStringProperty("prop");
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
		} catch (HornetQException e) {
			// TODO Auto-generated catch block
			System.out.println("read() Exception");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			markTaskComplete();
		}

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		try {
			consumer.close();
			session.commit();
			session.close();
			factory.close();
			logger.info("Subscriber closed");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("shutdown() Exception");
			e.printStackTrace();
		}
	}

}
