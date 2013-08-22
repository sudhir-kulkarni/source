package com.persistent.bcsuite.charts.objects;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;

/**
 * Graph context object that contains all the data necessary for the graph to be
 * generated.
 * 
 * 
 */
public class GraphContext {

	private static Logger logger = Logger.getLogger(Graph.class);
	private String title;
	private String xAxisLabel;
	private String yAxisLabel;
	private String graphFileName;
	private String strToken;

	/**
	 * 
	 * @param graphTitle
	 *            - title for the graph
	 * @param graphFileName
	 *            - File name for the graph
	 * @param xAxisLabel
	 *            - label for the xAxis
	 * @param yAxisLabel
	 *            - label for the yAxis
	 * @param strToken
	 *            - the database token for the dataset.
	 */
	public GraphContext(String graphTitle, String graphFileName,
			String xAxisLabel, String yAxisLabel, String strToken) {
		this.title = graphTitle;
		this.graphFileName = graphFileName
				+ BCSuiteConstants.FILE_TYPE_EXTN_PNG;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		this.strToken = strToken;
	}

	public String getStrToken() {
		return strToken;
	}

	public String getTitle() {
		return title;
	}

	public String getxAxisLabel() {
		return xAxisLabel;
	}

	public String getyAxisLabel() {
		return yAxisLabel;
	}

	public String getGraphFileName() {
		return graphFileName;
	}

	/**
	 * Returns the file object to which the graph file is to be written.
	 * 
	 * @return - file object
	 * @throws IOException
	 */
	public File getFile() throws IOException {
		logger.debug("Writing graph to file "
				+ BCSuiteConstants.FOLDER_PATH_BCSUITE + strToken
				+ BCSuiteConstants.FOLDER_PATH_IMAGE + getGraphFileName());
		File file = new File(BCSuiteConstants.FOLDER_PATH_BCSUITE + strToken
				+ BCSuiteConstants.FOLDER_PATH_IMAGE + getGraphFileName());
		file.getParentFile().mkdirs();
		file.createNewFile();
		return file;

	}
}
