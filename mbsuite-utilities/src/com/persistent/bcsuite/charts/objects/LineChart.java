package com.persistent.bcsuite.charts.objects;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.graphs.GraphTypes;
import com.persistent.bcsuite.charts.utils.MySqlDAO;
import com.persistent.bcsuite.charts.utils.ReportUtils;

/**
 * Generates a Line Chart for the given set of values. Saves the chart to a
 * file. Generates the HTML table for the data values
 * 
 */
public class LineChart implements Graph {

	private double[][] tableVal;
	private String title;
	private String filename;
	private String xLabel;
	private String yLabel;
	private String xAxisColName;
	private String yAxisColName;
	private String strToken;
	private GraphTypes graphType;
	private static MySqlDAO dao;

	public LineChart(String title, String filename, String xLabel,
			String yLabel, String xAxisColName, String yAxisColName,
			String strToken, GraphTypes graphType) {
		this.title = title;
		this.filename = filename+BCSuiteConstants.FILE_TYPE_EXTN_PNG;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.xAxisColName = xAxisColName;
		this.yAxisColName = yAxisColName;
		this.strToken = strToken;
		this.graphType = graphType;
		if (dao == null)
			dao = new MySqlDAO();
	}

	/**
	 * Method to plot the graph from the given data Saves the chart to a PNG
	 * file.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void plot() throws IOException, ClassNotFoundException, SQLException {
		XYDataset dataset = createDataset();
		JFreeChart chart = ChartFactory.createXYLineChart(this.title,
				this.xLabel, this.yLabel, dataset, PlotOrientation.VERTICAL,
				true, true, false);
		ChartUtilities.saveChartAsPNG(getFile(), chart,
				BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);

	}

	private XYSeriesCollection createDataset() throws IOException,
			ClassNotFoundException, SQLException {
		dao.init();
		tableVal = dao.getSummaryValueSet(graphType.toString(), xAxisColName,
				yAxisColName, strToken);
		XYSeries xyseried = new XYSeries(title);
		for (int i = 0; i < tableVal.length; i++) {
			int j = 0;
			XYDataItem dataItem = new XYDataItem(tableVal[i][j],
					tableVal[i][j + 1]);
			xyseried.add(dataItem);
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(xyseried);
		dao.cleanUp();
		return dataset;

	}

	/**
	 * Method for generating the HTML table for the data values in the graphs
	 * 
	 * @throws IOException
	 */
	public String generateTable() throws IOException {
		StringBuilder strBud = new StringBuilder();
		// main table td open
		strBud.append(BCSuiteConstants.TD_OPEN);
		strBud.append("<h1>");
		strBud.append(this.title);
		strBud.append("</h1>");
		strBud.append(BCSuiteConstants.TABLE_REPORT_OPEN);
		strBud.append(BCSuiteConstants.TH_OPEN);
		strBud.append(xLabel);
		strBud.append(BCSuiteConstants.TH_CLOSE);
		strBud.append(BCSuiteConstants.TH_OPEN);
		strBud.append(yLabel);
		strBud.append(BCSuiteConstants.TH_CLOSE);
		for (int i = 0; i < tableVal.length; i++) {
			strBud.append(BCSuiteConstants.TR_OPEN);
			int j = 0;
			strBud.append(BCSuiteConstants.TD_OPEN);
			strBud.append(tableVal[i][j]);
			strBud.append(BCSuiteConstants.TD_CLOSE);

			strBud.append(BCSuiteConstants.TD_OPEN);
			strBud.append(tableVal[i][j + 1]);
			strBud.append(BCSuiteConstants.TD_CLOSE);
			strBud.append(BCSuiteConstants.TR_CLOSE);
		}
		strBud.append(BCSuiteConstants.TABLE_CLOSE);
		strBud.append(BCSuiteConstants.TD_CLOSE);
		// main table td close
		// main table td open for appending image
		strBud.append(BCSuiteConstants.TD_OPEN);
		strBud.append("<img src=\"" + strToken
				+ BCSuiteConstants.FOLDER_PATH_IMAGE + filename
				+ "\" alt=\"graph\"> ");
		strBud.append(BCSuiteConstants.TD_CLOSE);
		// main table td close for appending image
		List<String> machineStatsToAppend = ReportUtils
				.loadMachineStatGraphs(strToken);
		for (String strFileName : machineStatsToAppend) {
			strBud.append(BCSuiteConstants.TD_OPEN);
			strBud.append("<img src=\"" + strToken
					+ BCSuiteConstants.FOLDER_PATH_IMAGE + strFileName
					+ "\" alt=\"graph\"> ");
			strBud.append(BCSuiteConstants.TD_CLOSE);
		}

		return strBud.toString();
	}

	/**
	 * Returns the file object to which the graph file is to be written.
	 * 
	 * @return - file object
	 * @throws IOException
	 */
	public File getFile() throws IOException {

		File file = new File(BCSuiteConstants.FOLDER_PATH_BCSUITE + strToken
				+ BCSuiteConstants.FOLDER_PATH_IMAGE + this.filename);
		file.getParentFile().mkdirs();
		file.createNewFile();
		return file;

	}
}
