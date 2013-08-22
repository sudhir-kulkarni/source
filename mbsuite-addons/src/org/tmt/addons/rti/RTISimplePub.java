package org.tmt.addons.rti;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmt.addons.rti.throughput.Throughput;
import org.tmt.addons.rti.throughput.ThroughputDataWriter;
import org.tmt.addons.rti.throughput.ThroughputTypeSupport;

import com.persistent.bcsuite.base.PublisherBase;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.ReliabilityQosPolicyKind;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.Publisher;
import com.rti.dds.publication.PublisherQos;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TopicQos;

public class RTISimplePub extends PublisherBase {

	public static Logger logger = Logger.getLogger(RTIPublisher.class);
	private static DomainParticipantFactory factory;
	private static DomainParticipant participant;
	private Publisher publisher;
	private Topic topic;
//	private StringDataWriter dataWriter;
	private ThroughputDataWriter dataWriter;
	private String topicName;
	private static int effort =0;

	boolean isReliable = false;
	private final Duration_t threeSecond = new Duration_t(3, 0);
	private static int counter = 0;
	
	 InstanceHandle_t data_instanceHandle = null;

	@Override
	public void init(Map<String, String> attributes) {
		super.init(attributes);
		String strEffort = attributes.get("effort");
		if(strEffort != null){
			effort = Integer.parseInt(strEffort);
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
		initPublisher();
		topicName = attributes.get("topic");
		System.out.println("Topic received "+topicName);
		// Create a topic from the message
		// if(topic ==null)
		createTopic(topicName);
		// Create the datawriter
		createDataWriter();
		System.out.println("Ready to write data.");
		System.out.print("Press CTRL+C to terminate\n\n");
		incrementCounter();
	}

	private synchronized void initFactory() {
		factory = DomainParticipantFactory.get_instance();
	}

	private synchronized void initDomainParticipant() {
		DomainParticipantQos participant_qos = new DomainParticipantQos();
		factory.get_default_participant_qos(participant_qos);  
		participant = factory.create_participant(0, // Domain ID = 0
				participant_qos, null, // listener
				StatusKind.STATUS_MASK_NONE);
		  ThroughputTypeSupport.register_type(participant,
                  ThroughputTypeSupport.get_type_name());
		  if (participant == null) {
			  System.out.println("Unable to create domain participant");
			  return;
		  }
	}

	private void initPublisher() {
		PublisherQos publisher_qos = new PublisherQos();
		participant.get_default_publisher_qos(publisher_qos);
		publisher = participant.create_publisher(publisher_qos, null,
				StatusKind.STATUS_MASK_NONE);
	}

	/**
	 * Create a topic from the message generated
	 */
	private void createTopic(String topicName) {
		System.out.println("Creating Topic " + topicName);
		 TopicQos data_topic_qos = new TopicQos();
		participant.get_default_topic_qos(data_topic_qos );
		Topic tempTopic = null;
		try {
			tempTopic = participant.create_topic(topicName,
                    ThroughputTypeSupport.get_type_name(), data_topic_qos,
                    null, StatusKind.STATUS_MASK_NONE);
		} catch (RETCODE_ERROR error) {
			System.out.println("Cannot Create topic. Topic already exists ");
		}
		if (tempTopic == null) {
			logger.debug("Topic already exists. Getting handle to topic");
			topic = participant.find_topic(topicName, threeSecond);
			logger.debug("Discovered Topic " + topic.get_name());
			System.out.println("Discovered Topic " + topic.get_name());
		} else {
			topic = tempTopic;
		}
	}

	private void createDataWriter() {
		logger.debug("Creating the Data Writer");
		// Create the data writer using the default publisher
		DataWriterQos datawriter_qos = new DataWriterQos();
		publisher.get_default_datawriter_qos(datawriter_qos);
		if (isReliable) {
			datawriter_qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
		}
		dataWriter = (ThroughputDataWriter) publisher.create_datawriter(topic,
				datawriter_qos, null, StatusKind.STATUS_MASK_NONE);
		 
		// dataWriter.enable();
		if (dataWriter == null) {
			logger.error("Unable to create data writer\n");
		}
	}

	@Override
	public void cleanup() {
		System.out.println("Exiting...");
		
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

	@Override
	public void sendMessageForThroughput(byte[] message) {
		int len = message.length;
		int exceptionCnt = 0;
		long messageCounter = 0;
		Throughput throughput = new Throughput(message.length);
		throughput.data.addAllByte(message);
		  data_instanceHandle = dataWriter.register_instance(throughput);
		setStartTime(new Date());
		while (canContinue()) {
			try {
//				for (int l = 0; l < 20000 && canContinue; l++) {
					try {
						dataWriter.write(throughput ,
								InstanceHandle_t.HANDLE_NIL);
						messageCounter++;
						addToStatisticsPool(len);
					} catch (RETCODE_ERROR e) {
						if(exceptionCnt <=5){
							e.printStackTrace();
						}
						exceptionCnt++;
						
					}
//				}
				if (getThinkTimeInMillis() > 0) {
					try {
						Thread.sleep(getThinkTimeInMillis());
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} catch (Exception ex) {
				logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = "
						+ ex);
			}
		}

		setEndTime(new Date());
		System.out.println("Sleeping for 8 seconds");
		System.out.println("Exceptions " + exceptionCnt);
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			markTaskComplete();
		}
	}

	@Override
   public void sendMessageForLatency(byte[] message) {
	   String s = new String(message);
      int len = message.length;
      int exceptionCnt = 0;
      long messageCounter = 0;
      Throughput throughput = new Throughput(message.length);
      
        data_instanceHandle = dataWriter.register_instance(throughput);
      setStartTime(new Date());
      while (canContinue()) {
         try {
               try {
                  throughput.data.clear();
                  long nanoTime = System.nanoTime();
                  throughput.data.addAllByte((String.valueOf(nanoTime) + ":" +s).getBytes());
                  dataWriter.write(throughput ,
                        InstanceHandle_t.HANDLE_NIL);
                  messageCounter++;
                  addToStatisticsPool(len);
               } catch (RETCODE_ERROR e) {
                  if(exceptionCnt <=5){
                     e.printStackTrace();
                  }
                  exceptionCnt++;                  
               }
            if (getThinkTimeInMillis() > 0) {
               try {
                  Thread.sleep(getThinkTimeInMillis());
               } catch (InterruptedException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
               }
            }
         } catch (Exception ex) {
            logger.error("Aborting Publisher -- Recieved exception during sending message. Exception is = "
                  + ex);
         }
      }

      setEndTime(new Date());
      System.out.println("Sleeping for 8 seconds");
      System.out.println("Exceptions " + exceptionCnt);
      try {
         Thread.sleep(8000);
      } catch (InterruptedException e1) {
         e1.printStackTrace();
      } finally {
         markTaskComplete();
      }
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
	
}
