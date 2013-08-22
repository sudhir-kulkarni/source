package com.persistent.bcsuite.charts.objects;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.graphs.GraphTypes;
import com.persistent.bcsuite.charts.utils.MySqlDAO;
import com.persistent.bcsuite.charts.utils.ReportUtils;

/**
 * Generates a Scatter Chart for the given set of values. Saves the chart to a
 * file. Generates the HTML table for the data values
 * 
 */
public class ScatterChart implements Graph {

	private static MySqlDAO dao;
	HashMap<String, Double[][]> values;
	private String title;
	private String filename;
	private String xLabel;
	private String yLabel;
	private String xAxisColName;
	private String yAxisColName;
	private String strToken;
	private GraphTypes graphType;

	public ScatterChart(String title, String filename, String xLabel,
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
	 * Method to plot the graph from the given data. Saves the chart to a PNG
	 * file.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@Override
	public void plot() throws IOException, ClassNotFoundException, SQLException {
		JFreeChart chart = ChartFactory.createScatterPlot(this.title,
				this.xLabel, this.yLabel, createDataset(),
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyPlot = (XYPlot) chart.getPlot();
/*		NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();

		domain.setTickUnit(new NumberTickUnit(100));
		domain.setVerticalTickLabels(true);
		NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
		rangeAxis.setTickUnit(new NumberTickUnit(500));*/
		ChartUtilities.saveChartAsPNG(getFile(), chart,
				BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);
	}

	private XYDataset createDataset() throws IOException,
			ClassNotFoundException, SQLException {
		dao.init();
		double[][] values = dao.getSummaryValueSet(graphType.toString(),
				xAxisColName, yAxisColName, strToken);
		HashMap<String, Double[][]> scatterValueMap = new HashMap<String, Double[][]>();
		scatterValueMap.put(this.title,
				ReportUtils.convertdoubleToDouble(values));
		Set<String> keySet = scatterValueMap.keySet();
		XYSeriesCollection result = new XYSeriesCollection();
		for (String key : keySet) {
			XYSeries series = new XYSeries(key);
			Double[][] arrValues = scatterValueMap.get(key);
			for (int i = 0; i < arrValues.length; i++) {
				int j = 0;
				series.add(arrValues[i][j], arrValues[i][j + 1]);
			}
			result.addSeries(series);
		}
		dao.cleanUp();
		return result;

	}

	/**
	 * Get the valueset for chart
	 * 
	 * @return - Values for this chart
	 */
	public HashMap<String, Double[][]> getValues() {
		return values;
	}

	/**
	 * Method for generating the HTML table for the data values in the graphs
	 * 
	 * @throws IOException
	 */
	@Override
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
		if (values != null) {
			Set<String> mapKeySet = values.keySet();
			for (String mapKey : mapKeySet) {
				Double[][] tableVal = values.get(mapKey);
				for (int i = 0; i < tableVal.length; i++) {
					int j = 0;
					strBud.append(BCSuiteConstants.TR_OPEN);
					strBud.append(BCSuiteConstants.TD_OPEN);
					strBud.append(tableVal[i][j]);
					strBud.append(BCSuiteConstants.TD_CLOSE);

					strBud.append(BCSuiteConstants.TD_OPEN);
					strBud.append(tableVal[i][j + 1]);
					strBud.append(BCSuiteConstants.TD_CLOSE);
					strBud.append(BCSuiteConstants.TR_CLOSE);
				}
			}
		}
		strBud.append(BCSuiteConstants.TABLE_CLOSE);
		// main table td close
		strBud.append(BCSuiteConstants.TD_CLOSE);
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

	public static MySqlDAO getDao() {
		return dao;
	}

	public String getTitle() {
		return title;
	}

	public String getFilename() {
		return filename;
	}

	public String getxLabel() {
		return xLabel;
	}

	public String getyLabel() {
		return yLabel;
	}

	public String getxAxisColName() {
		return xAxisColName;
	}

	public String getyAxisColName() {
		return yAxisColName;
	}

	public String getStrToken() {
		return strToken;
	}

	public GraphTypes getGraphType() {
		return graphType;
	}
}
