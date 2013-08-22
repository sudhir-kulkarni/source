package com.persistent.bcsuite.addons;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;

import com.persistent.bcsuite.base.PublisherBase;

public class HornetqPublisher extends PublisherBase {

	private static final Logger logger = Logger
			.getLogger(HornetqPublisher.class.getName());
	private String address;
	private String serverHost;
	private String serverPort;
	private String tcpBuffer;
	private String tcpNoDelay;
	private ClientSessionFactory factory;
	private ClientProducer producer;
	private ClientSession session = null;
	private ClientMessage msg;
	private String preAck;
	private ServerLocator serverLocator;

	@Override
	public void init(Map<String, String> attributes) {
		logger.info("HornetqPublisher Initializing");
		super.init(attributes);
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
		initializeMessagePlatform();
		logger.info("Sample Publisher initialization complete...");
	}

	private void initializeMessagePlatform() {
		// TODO Auto-generated method stub
		try {
			factory = serverLocator.createSessionFactory();
			session = factory.createSession();
			producer = session.createProducer(address);
		} catch (HornetQException e) {
			// TODO Auto-generated catch block
			System.out.println("init() Exception");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("HornetqPublisher inited");

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
		String m = new String(message);
		try {
			while (canContinue) {
				// send messages till the suite does not tell you to shutdown
				sendMessage(m, message);

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

	private void sendMessage(String m, byte[] message) {
		// TODO Auto-generated method stub
		try {

			msg = session.createMessage(false);
			msg.putStringProperty("prop", m);
			producer.send(msg);
			// logger.info("message sent");
		} catch (HornetQException e) {
			// TODO Auto-generated catch block
			System.out.println("sendMessage() Exception");
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
			int mcounter=0;
			while (canContinue) {
				// send messages till the suite does not tell you to shutdown

				// Generate unique identifier for each message
				i++;
				mcounter++;
				String messageIdentifier = getMessagePrefix() + i;
				// prepend the identifier to the message. Get the exact number
				// of bytes that were requested in the config.
				String messageToSend = (messageIdentifier + ":" + mstr).substring(0,
						getMessageLength());
				
				
				Date sentDate = new Date();
				// Send the message. Now the message contains a unique
				// identifier which is used for calculating latency.
				sendMessage( messageToSend,mstr.getBytes());

				// add data to statistics pool. This will count the number of
				// messages and the total bytes sent.
				addToStatisticsPool(messageIdentifier, sentDate, 1,
						messageToSend.length());
						//if mcounter is 999
				if(mcounter==999)
				{
					addLatencyStatistics(String.valueOf(mcounter), 2);
					mcounter=0;
				}
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
		try {

			producer.close();
			session.close();
			factory.close();
			logger.info("Publisher closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("cleanup() Exception");
			e.printStackTrace();
		}

	}


}
