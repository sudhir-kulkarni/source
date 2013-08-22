package com.persistent.bcsuite.charts.objects;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.graphs.GraphTypes;
import com.persistent.bcsuite.charts.utils.MySqlDAO;
import com.persistent.bcsuite.charts.utils.ReportUtils;

/**
 * Generates a Pie Chart for the given set of values. Saves the chart to a file.
 * Generates the HTML table for the data values
 * 
 */
public class PieChart implements Graph {

	private Map<String, Double> values;
	private String title;
	private String filename;
	private String strToken;
	private GraphTypes graphType;
	private static MySqlDAO dao;

	public PieChart(String title, String filename, String strToken,
			GraphTypes graphType) {
		this.title = title;
		this.filename = filename+BCSuiteConstants.FILE_TYPE_EXTN_PNG;
		this.strToken = strToken;
		this.graphType = graphType;
		if (dao == null)
			dao = new MySqlDAO();
	}

	/**
	 * Constructor for the PieChart
	 * 
	 * @param graphContext
	 *            - the graphContext object
	 */
	public PieChart(String title) {
		this.title = title;
	}

	/**
	 * Method to plot the graph from the given data Saves the chart to a PNG
	 * file.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@Override
	public void plot() throws IOException, ClassNotFoundException, SQLException {
		PieDataset dataset = createDataset();

		JFreeChart chart = ChartFactory.createPieChart(this.title, dataset,
				true, true, false);
		ChartUtilities.saveChartAsPNG(getFile(), chart,
				BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);

	}

	private PieDataset createDataset() throws IOException,
			ClassNotFoundException, SQLException {
		dao.init();
		values = dao.getPieValueSet(graphType.toString(), strToken);
		DefaultPieDataset pieDataSet = new DefaultPieDataset();
		if (values != null) {
			Set<String> keySet = values.keySet();
			for (String key : keySet) {
				Double value = values.get(key);
				pieDataSet.setValue(key, value.doubleValue());
			}
		}
		dao.cleanUp();
		return pieDataSet;
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
		strBud.append(this.title);
		strBud.append(BCSuiteConstants.TH_CLOSE);
		if (values != null) {
			Set<String> mapKeySet = values.keySet();
			for (String mapKey : mapKeySet) {
				strBud.append(BCSuiteConstants.TR_OPEN);
				strBud.append(BCSuiteConstants.TD_OPEN);
				strBud.append(mapKey);
				strBud.append(BCSuiteConstants.TD_CLOSE);
				strBud.append(BCSuiteConstants.TD_OPEN);
				strBud.append(values.get(mapKey));
				strBud.append(BCSuiteConstants.TD_CLOSE);
				strBud.append(BCSuiteConstants.TR_CLOSE);

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

}
