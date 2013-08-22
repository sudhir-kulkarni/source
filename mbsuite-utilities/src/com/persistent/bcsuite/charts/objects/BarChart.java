package com.persistent.bcsuite.charts.objects;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;

/**
 * Generates a Bar Chart for the given set of values. Saves the chart to a file.
 * Generates the HTML table for the data values.
 */
public class BarChart implements Graph {

	private HashMap<String, HashMap<String, Double>> values;
	private String title;
	private String filename;
	private String xLabel;
	private String yLabel;
	private String strToken;

	public BarChart(String title, String filename, String xLabel,
			String yLabel, String strToken) {
		this.title = title;
		this.filename = filename+BCSuiteConstants.FILE_TYPE_EXTN_PNG;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.strToken = strToken;

	}

	/**
	 * Get the valueset for chart
	 * 
	 * @return - Values for this chart
	 */
	public HashMap<String, HashMap<String, Double>> getValues() {
		return values;
	}

	/**
	 * Set the valueset for chart
	 * 
	 * @param valueMap
	 *            - Values for plotting the graph
	 */
	public void setValues(HashMap<String, HashMap<String, Double>> valueMap) {
		this.values = valueMap;
	}

	/**
	 * Method to plot the graph from the given data Saves the chart to a PNG
	 * file.
	 * 
	 * @throws IOException
	 */
	@Override
	public void plot() throws IOException {

		JFreeChart chart = ChartFactory.createBarChart("Bar Chart",
				xLabel, yLabel,
				createDataSet(), PlotOrientation.VERTICAL, true, true, false);
		ChartUtilities.saveChartAsPNG(getFile(), chart,
				BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);

	}

	private CategoryDataset createDataSet() {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		if (values != null) {
			Set<String> mapKeySet = values.keySet();
			for (String mapKey : mapKeySet) {
				Map<String, Double> categoryMap = values.get(mapKey);
				if (categoryMap != null) {
					Set<String> catKeySet = categoryMap.keySet();
					for (String catKey : catKeySet) {
						Double numValue = categoryMap.get(catKey);
						dataset.addValue(numValue, mapKey, catKey);
					}
				}
			}
		}
		return dataset;
	}

	/**
	 * Method for generating the HTML table for the data values in the graphs
	 */
	@Override
	public String generateTable() {
		StringBuilder strBud = new StringBuilder();
		strBud.append(BCSuiteConstants.TD_OPEN);
		strBud.append("<h1>");
		strBud.append(this.title);
		strBud.append("</h1>");
		strBud.append(BCSuiteConstants.TABLE_REPORT_OPEN);
		strBud.append(BCSuiteConstants.TH_OPEN);
		strBud.append(title);
		strBud.append(BCSuiteConstants.TH_CLOSE);
		if (values != null) {
			Set<String> mapKeySet = values.keySet();
			for (String mapKey : mapKeySet) {
				strBud.append(BCSuiteConstants.TR_OPEN);
				strBud.append(BCSuiteConstants.TD_OPEN);
				strBud.append(mapKey);
				strBud.append(BCSuiteConstants.TD_CLOSE);
				strBud.append(BCSuiteConstants.TR_CLOSE);
				Map<String, Double> categoryMap = values.get(mapKey);
				if (categoryMap != null) {
					Set<String> catKeySet = categoryMap.keySet();
					for (String catKey : catKeySet) {
						strBud.append(BCSuiteConstants.TR_OPEN);
						strBud.append(BCSuiteConstants.TD_OPEN);
						strBud.append(catKey);
						strBud.append(BCSuiteConstants.TD_CLOSE);
						strBud.append(BCSuiteConstants.TD_OPEN);
						Double numValue = categoryMap.get(catKey);
						strBud.append(numValue);
						strBud.append(BCSuiteConstants.TD_CLOSE);
						strBud.append(BCSuiteConstants.TR_CLOSE);
					}
				}
			}
		}
		strBud.append(BCSuiteConstants.TABLE_CLOSE);
		strBud.append(BCSuiteConstants.TD_CLOSE);
		//main table td open for appending image
		strBud.append(BCSuiteConstants.TD_OPEN);
		strBud.append("<img src=\"" + strToken
				+ BCSuiteConstants.FOLDER_PATH_IMAGE
				+ filename
				+ "\" alt=\"graph\"> ");
		strBud.append(BCSuiteConstants.TD_CLOSE);
		//main table td close for appending image
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
