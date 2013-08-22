package com.persistent.bcsuite.charts.objects;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
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
import com.persistent.bcsuite.charts.utils.ReportUtils;

public class LatencyPercentileChart extends ScatterChart {

	public LatencyPercentileChart(String title, String filename, String xLabel,
			String yLabel, String xAxisColName, String yAxisColName,
			String strToken, GraphTypes graphType) {
		super(title, filename, xLabel, yLabel, xAxisColName, yAxisColName,
				strToken, graphType);

	}

	@Override
	public void plot() throws IOException, ClassNotFoundException, SQLException {

		JFreeChart chart = ChartFactory.createScatterPlot(getTitle(), getxLabel(),
				getyLabel(), createDataset(), PlotOrientation.VERTICAL, true, true,
				false);
		XYPlot xyPlot = (XYPlot) chart.getPlot();
		/*NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();

		domain.setTickUnit(new NumberTickUnit(100));
		domain.setVerticalTickLabels(true);
		NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
		rangeAxis.setTickUnit(new NumberTickUnit(500));*/
		ChartUtilities.saveChartAsPNG(getFile(), chart,
				BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);

	}

	private XYDataset createDataset() throws IOException,
			ClassNotFoundException, SQLException {
		getDao().init();
		double[][] values = getDao().getSummaryValueSet(
				getGraphType().toString(), getxAxisColName(),
				getyAxisColName(), getStrToken());

		double[] distribution = ReportUtils.getAxisValueSet(BCSuiteConstants.YAXIS, values);
		double[][] ltncyDistriValues = ReportUtils
				.calculatePercentile(distribution);
		HashMap<String, Double[][]> scatterPercentilValueMap = new HashMap<String, Double[][]>();
		scatterPercentilValueMap.put(getTitle(),
				ReportUtils.convertdoubleToDouble(ltncyDistriValues));
		Set<String> keySet = scatterPercentilValueMap.keySet();
		XYSeriesCollection result = new XYSeriesCollection();
		for (String key : keySet) {
			XYSeries series = new XYSeries(key);
			Double[][] arrValues = scatterPercentilValueMap.get(key);
			for (int i = 0; i < arrValues.length; i++) {
				int j = 0;
				series.add(arrValues[i][j], arrValues[i][j + 1]);
			}
			result.addSeries(series);
		}
		getDao().cleanUp();
		return result;

	}

}
