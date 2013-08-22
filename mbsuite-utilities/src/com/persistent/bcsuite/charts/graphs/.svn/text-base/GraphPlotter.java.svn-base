package com.persistent.bcsuite.charts.graphs;

import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.persistent.bcsuite.charts.objects.Graph;

/**
 * Class for plotting the graphs.
 * 
 */
public class GraphPlotter {

	private static Logger logger = Logger.getLogger(GraphPlotter.class);

	/**
	 * Method for plotting the graph for a given set of values.
	 * 
	 * @param graphs
	 *            - List of Graph Object(s)
	 * @throws Exception 
	 */
	public void plotGraph(List<Graph> graphs) throws Exception {

		if (graphs != null && !graphs.isEmpty()) {
			for (Graph graph : graphs) {
				graph.plot();

			}
		} else {
			logger.error("Null or Empty graphs list received.");
		}

	}

	/**
	 * Method to create the dataset for the graph from given array of values and
	 * title
	 * 
	 * @param values
	 *            - array of values
	 * @param title
	 *            - title of graph
	 * @return - the XYSeriesCollection required by JFreechart
	 */
	public XYSeriesCollection createDataset(double[][] values, String title) {
		XYSeries xyseried = new XYSeries(title);
		for (int i = 0; i < values.length; i++) {
			int j = 0;
			XYDataItem dataItem = new XYDataItem(values[i][j], values[i][j + 1]);
			xyseried.add(dataItem);
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(xyseried);
		return dataset;

	}

}
