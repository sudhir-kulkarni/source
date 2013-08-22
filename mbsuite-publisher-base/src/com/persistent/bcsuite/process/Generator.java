package com.persistent.bcsuite.process;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.PublisherBase;
import com.persistent.bcsuite.support.DataStore;
import com.persistent.bcsuite.support.FileStore;
import com.persistent.bcsuite.support.LoadDistributionManager;
import com.persistent.bcsuite.support.PublisherStatistics;
import com.persistent.bcsuite.support.ValidatorUtil;

public class Generator extends BaseGenerator {
	private static final Logger logger = Logger.getLogger(Generator.class
			.getName());
	private int totalPublisherThreads = 0;
	private int messageSizeInBytes = 0;
	private double elapsedTimeInSecs;
	private double mbBytesSent;
	private double numOfMessagesPerSec;
	private double mbsPerSec;
	private int totalMessagesSent = 0;
	private Date publisherProcessStartTime = new Date(Long.MAX_VALUE);
	private Date publisherProcessEndTime = new Date(0);
	private String[] topicArray = null;
	private int warmupTimeInMillis = 0;
	List<PublisherBase> tasksList = null;

	// The values below are not to be reset during repeated runs.
	int runCounter = 0;
	private boolean possibleHangingThreads = false;

	public Generator(String key, String testgroup, String token,
			int repeatCounter) {
		super(key, testgroup, token, repeatCounter);
	}

	/**
	 * Entry Point for the Load Generator
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		if (!ValidatorUtil.areArgumentsValid(args))
			return;

		logger.info("################################ Publisher Test Process started ################################ ");
		logger.info("Using command line parameters:[taskkey=" + args[0]
				+ "],[testgroupname=" + args[1] + "],[token=" + args[2] + "].");

		int repeatCounter = getRepeatCounter(args);
		Generator generator = new Generator(args[0], args[1], args[2],
				repeatCounter);
		for (int i = 0; i < repeatCounter; i++) {
			if (generator.isPossibleHangingThreads()) {
				logger.error("Earlier run of the test resulted in possible hanged threads of publishers. Cannot continue multiple run.");
				break;
			}
			System.gc();
			generator.process();
		}
		logger.info("################################ Test Process finished ################################ ");
		System.exit(0);
	}

	/**
	 * This method is responsible for allocating publishers to topic,
	 * instantiating the publisher programs invoking init and other methods,
	 * allocating a thread to each publisher and then eventually executing them
	 * to allow to send messages. This will also handle the shutdown of all
	 * publishers and will collect the statistics and save to the database.
	 * 
	 * The method can run multiple number of times in case the invoker wants.
	 * 
	 * @throws Exception
	 *             - Any exception encountered during the run, the process has
	 *             to come out.
	 */
	private void process() throws Exception {

		try {
			logger.info("\nTest Run [" + ++runCounter
					+ "] started >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

			findPublisherGroup(publisherSettings);
			if (!ValidatorUtil.isGroupValid(pg))
				return;

			if (runCounter == 1
					|| "cold".equalsIgnoreCase(pg.getIterationType())) {
				tasksList = new ArrayList<PublisherBase>();
				resetValues();

				totalPublisherThreads = pg.getTotalPublishers();
				messageSizeInBytes = pg.getMessageSize();
				warmupTimeInMillis = pg.getWarmUpTimeInMillis();

				Map<String, String> tasks = publisherSettings.getTasks(); // This
																			// fetches
																			// all
																			// the
																			// publisher
																			// programs
																			// that
																			// are
																			// configured
																			// in
																			// the
																			// config
																			// file.

				topicArray = generateTopicArray(pg.getNumTopics(), key);
				byte[] arr = getByteArray(pg.getMessageSize()); // Generate a
																// message of
																// the required
																// length. The
																// message is a
																// array of
																// bytes.

				Map<String, Integer> loadDistribution = LoadDistributionManager
						.generateLoadDistribution(topicArray,
								pg.getTotalPublishers(),
								pg.getLoadDistStrategy());

				StringBuilder sb = new StringBuilder(
						"******** Load Distribution Details ************\n");

				// the 3 variables below are used to generate a unique string
				// identifying the publisher to a topic.
				String pubPrefixString = "P";
				String topicPrefixStrig = "T";
				int topicNumber = 0;

				// The loop below distributes the publishers to the topics based
				// on the load distribution calculated.
				for (String tp : topicArray) {
					topicNumber++;
					int totalPublishersForThisTopic = loadDistribution.get(tp);
					pg.getAttributes().put("topic", tp);
					for (int i = 0; i < totalPublishersForThisTopic; i++) {
						String pubPrefix = pubPrefixString + i
								+ topicPrefixStrig + topicNumber;
						PublisherBase t = getTask(tasks.get(key));
						pg.getAttributes().put("thinkTime",
								String.valueOf(pg.getThinkTimeInMillis()));

						// Call init on the publisher program
						t.init(pg.getAttributes());

						// allow publisher program to prepare itself before run.
						t.prepareToRun(arr, pg.isDumpDetails(), pubPrefix,
								pg.getMessageSize());

						// Add it to our tracking list. DONT start...all
						// publishers need to start at almost the same time.
						tasksList.add(t);
					}
					sb.append("[").append(totalPublishersForThisTopic)
							.append("] publishers sending message to [")
							.append(tp).append("]\n");
				}
				logger.info("Load Distribution Strategy = " + loadDistribution);
				logger.info(sb.toString());

				// If a warmup time is specified the suite sleeps for that
				// particular time.
				Thread.sleep(warmupTimeInMillis);
			}// if(repeatCounter == 1 ||
				// "cold".equalsIgnoreCase(pg.getIterationType()))
			else {
				logger.info("Executing this test run as a hot iteration");
			}

			areAllTasksReadyToRun(tasksList);

			logger.info("Instructing all publishers to start sending messages");
			for (PublisherBase task : tasksList) {
				task.prepareForIteration();
				Thread t = new Thread(task);
				t.start(); // Start the publisher instances now. Each instance
							// has its own thread.
			}

			logger.info("All publishers are now sending messages. Will wait for specified run time ["
					+ pg.getRunForSeconds() + "] secs.");
			Thread.sleep(pg.getRunForSeconds() * 1000);

			for (PublisherBase task : tasksList) {
				if (repeatCounter == 1) {
					logger.info("Total run time elapsed. Sending shutdown interrupt to all publishers");
					task.shutdown(); // Inform all publishers that they should
										// stop sending messages.
				} else if (runCounter == repeatCounter) {
					logger.info("All hot iterations completed. Sending shutdown interrupt to all publishers");
					task.shutdown(); // Inform all publishers that they should
										// stop sending messages.
				} else if (repeatCounter > 1
						&& "cold".equalsIgnoreCase(pg.getIterationType())) {
					logger.info("Total run time elapsed.Cold iteration expected, sending shutdown signal to all publishers");
					task.shutdown(); // The program will iterate but it has to
										// do a cold iteration. Close everything
										// and start all over again
				} else if (repeatCounter > 1
						&& "hot".equalsIgnoreCase(pg.getIterationType())) {
					logger.info("Total run time elapsed.Hot iteration, cannot shutdown publishers, informing them to get ready for next iteration...");
					task.getReadyForNextIteration();
				}
			}

			// Wait till everyone acknowledges and finishes their activities
			waitForTasksToFinish(tasksList);

			if (pg.getIterationsToIgnore() < runCounter) {
				// Collect the statistics and dump them to the DB for future
				// reporting and analysis.
				collectAndSave(tasksList);
			} else {
				logger.info("Ignoring this test run, not saving data to DB");
			}

			if (pg.getIterationDrainTimeInMillis() > 0) {
				logger.info("Will wait for specified iteration drain time ["
						+ pg.getIterationDrainTimeInMillis() + "] ms");
				Thread.sleep(pg.getIterationDrainTimeInMillis());
			}

			logger.info("Calling cleanup on all publishers");
			for (PublisherBase task : tasksList) {
				try {
					if (repeatCounter == 1) {
						logger.info("Total run time elapsed. calling cleanup on all publishers");
						task.cleanup(); // Inform all publishers that they
										// should stop sending messages.
					} else if (runCounter == repeatCounter) {
						logger.info("All hot iterations completed. Calling cleanup on all publishers");
						task.cleanup(); // Inform all publishers that they
										// should stop sending messages.
					} else if (repeatCounter > 1
							&& "cold".equalsIgnoreCase(pg.getIterationType())) {
						logger.info("Total run time elapsed.Cold iteration expected, calling cleanup on all publishers");
						task.cleanup(); // The program will iterate but it has
										// to do a cold iteration. Close
										// everything and start all over again
					} else if (repeatCounter > 1
							&& "hot".equalsIgnoreCase(pg.getIterationType())) {
						logger.info("Total run time elapsed.Hot iteration, will not invoke cleanup on Publishers");
					}
				} catch (Throwable t) {
				}
			}

			logger.info("Test Run [" + runCounter
					+ "] finished <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		} catch (Throwable t) {
			if (tasksList != null) {
				for (PublisherBase publisherBase : tasksList) {
					try {
						publisherBase.shutdown();
						publisherBase.cleanup();
					} catch (Exception e) {
					}
				}
				tasksList.clear();
			}
			t.printStackTrace();
			throw new RuntimeException("Encountered error in process method");

		}
	}

	private String[] generateTopicArray(int numTopics, String key) {
		ArrayList<String> topicArr = new ArrayList<String>();

		logger.debug("Creating [" + numTopics + "] in topicArray");
		for (int i = 0; i < numTopics; i++) {
			topicArr.add(key + "-topic" + i);
		}
		Collections.sort(topicArr);
		logger.debug("topic array contents = [" + topicArr + "]");
		return (String[]) topicArr.toArray(new String[1]);
	}

	/**
	 * Fetch the data from the statistics and persist to DB using the utility
	 * method.
	 * 
	 * @param allPublishers
	 *            - List of all publisher instances.
	 * @throws SQLException
	 */
	private void collectAndSave(List<PublisherBase> allPublishers)
			throws SQLException {
		long totalBytesSent = 0;
		totalMessagesSent = 0;
		publisherProcessStartTime = new Date(Long.MAX_VALUE);
		publisherProcessEndTime = new Date(0);
		for (PublisherBase task : allPublishers) {
			PublisherStatistics ps = task.getPublisherStatistics();

			if (ps.getProcessStartTime().getTime() < publisherProcessStartTime
					.getTime())
				publisherProcessStartTime = ps.getProcessStartTime();

			if (ps.getProcessEndTime().getTime() > publisherProcessEndTime
					.getTime())
				publisherProcessEndTime = ps.getProcessEndTime();

			totalMessagesSent += ps.getTotalMessagesSent();
			totalBytesSent += ps.getTotalBytesSent();
		}

		elapsedTimeInSecs = (double) (publisherProcessEndTime.getTime() - publisherProcessStartTime
				.getTime()) / 1000;
		mbBytesSent = (double) (totalBytesSent) / (1024 * 1024);
		numOfMessagesPerSec = (double) totalMessagesSent / elapsedTimeInSecs;
		mbsPerSec = (double) mbBytesSent / elapsedTimeInSecs;

		HashMap<String, Object> p = new HashMap<String, Object>();
		p.put("instancetoken", instanceToken);
		p.put("runcounter", runCounter);
		p.put("processstarttime", publisherProcessStartTime.getTime());
		p.put("processendtime", publisherProcessEndTime.getTime());
		p.put("totalpublisherthreads", totalPublisherThreads);
		p.put("messagesizeinbytes", messageSizeInBytes);
		p.put("totalmsgsent", totalMessagesSent);
		p.put("noofmsgpersec", numOfMessagesPerSec);
		p.put("mbsent", mbBytesSent);
		p.put("mbpersec", mbsPerSec);
		p.put("testgroup", testgroup);
		p.put("taskkey", key);

		if ("db".equalsIgnoreCase(BaseGenerator.dumpDestination)
				|| dumpDestination == null
				|| dumpDestination.trim().length() == 0) {
			
			DataStore.saveSummary(dbUrl, pg.getAttributes(), p);

			if (pg.isDumpDetails()) {
				for (PublisherBase task : allPublishers) {
					logger.info("Dumping details to DB with token ["
							+ instanceToken + "]");
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("instancetoken", instanceToken);
					params.put("runcounter", String.valueOf(runCounter));
					DataStore.saveDetails(dbUrl, task.getPublisherStatistics()
							.getDetailData(), params);
				}
			}

		} else {
			URL location =  this.getClass().getResource("/csv");
			FileStore.saveSummary(pg.getAttributes(), p , location);

			if (pg.isDumpDetails()) {
				for (PublisherBase task : allPublishers) {
					logger.info("Dumping details to file with token ["
							+ instanceToken + "]");
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("instancetoken", instanceToken);
					params.put("runcounter", String.valueOf(runCounter));
					FileStore.saveDetails(task.getPublisherStatistics()
							.getDetailData(), params,location);

				}
			}
		}
	}

	/**
	 * Reset attributes required in repeated runs.
	 */
	private void resetValues() {
		totalPublisherThreads = 0;
		messageSizeInBytes = 0;
		elapsedTimeInSecs = 0;
		mbBytesSent = 0;
		numOfMessagesPerSec = 0;
		mbsPerSec = 0;
		totalMessagesSent = 0;
		publisherProcessStartTime = new Date(Long.MAX_VALUE);
		publisherProcessEndTime = new Date(0);
		topicArray = null;
		warmupTimeInMillis = 0;
	}

	/**
	 * Wait for tasks to finish
	 * 
	 * @param tasksList
	 * @throws Exception
	 */
	private void waitForTasksToFinish(List<PublisherBase> tasksList)
			throws Exception {
		boolean allTasksFinished = false;
		while (!allTasksFinished) {
			allTasksFinished = true;
			for (PublisherBase task : tasksList) {
				if (!task.isTaskComplete())
					allTasksFinished = false;
			}
			logger.info("Waiting for publishers to cleanly shutdown....");
			Thread.sleep(2000);
		}
		if (allTasksFinished)
			logger.info("All Publishers have shutdown.");
	}

	/**
	 * Wait for tasks to finish
	 * 
	 * @param tasksList
	 * @throws Exception
	 */
	private void areAllTasksReadyToRun(List<PublisherBase> tasksList)
			throws Exception {
		boolean allTasksReady = false;
		logger.info("Waiting for publishers to get themselves ready for run....");
		while (!allTasksReady) {
			allTasksReady = true;
			for (PublisherBase task : tasksList) {
				if (!task.isReadyToRun()) {
					allTasksReady = false;
				}
			}

			Thread.sleep(100);
		}
		if (allTasksReady)
			logger.info("All Publishers are now ready for run");
	}

	public boolean isPossibleHangingThreads() {
		return possibleHangingThreads;
	}

}
