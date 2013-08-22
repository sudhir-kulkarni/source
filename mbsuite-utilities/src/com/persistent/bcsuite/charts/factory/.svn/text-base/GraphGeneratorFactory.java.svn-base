package com.persistent.bcsuite.charts.factory;

import java.io.IOException;
import java.util.Map;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.graphs.GraphTypes;
import com.persistent.bcsuite.charts.objects.GenericBarChart;
import com.persistent.bcsuite.charts.objects.GenericLineChart;
import com.persistent.bcsuite.charts.objects.Graph;
import com.persistent.bcsuite.charts.objects.LatencyPercentileChart;
import com.persistent.bcsuite.charts.objects.LineChart;
import com.persistent.bcsuite.charts.objects.PieChart;
import com.persistent.bcsuite.charts.objects.ScatterChart;
import com.persistent.bcsuite.charts.utils.ReportUtils;

/**
 * GraphGeneratorFactory returns an instance of graph object based on the type of graph.
 * 
 */
public class GraphGeneratorFactory {

   private static GraphGeneratorFactory graphFactory;

   private GraphGeneratorFactory() {

   }

   public static GraphGeneratorFactory getInstance() {
      if (graphFactory == null)
         graphFactory = new GraphGeneratorFactory();
      return graphFactory;
   }

   public Graph getGraph(String strToken, String reportType) {
      Graph g = null;
      try {
         Map<String, String> reportTypes = ReportUtils.loadConfig(BCSuiteConstants.COLLECTOR_CONFIG_FILE);
         System.out.println("report types = " + reportTypes);
         if ("line".equalsIgnoreCase(reportTypes.get(reportType))) {
            GenericLineChart genericLineChart = new GenericLineChart(reportType, strToken);
            g = genericLineChart;
         } else if ("bar".equalsIgnoreCase(reportTypes.get(reportType))) {
            GenericBarChart genericBarChart = new GenericBarChart(reportType, strToken);
            g = genericBarChart;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return g;
   }

   /**
    * Returns a graph based on graphtype.
    * 
    * @param graphType
    *           - GraphType
    * @param strToken
    *           - database token
    * @return
    */
   public Graph getGraph(GraphTypes graphType, String strToken) {
      String xLabel;
      String yLabel;
      String title;
      switch (graphType) {
      /*
       * case PVSIZE: xLabel = "Message Size (bytes)"; yLabel = "Throughput (Msg/sec)"; title =
       * "Publisher throughput with varying messaging size "; LineChart pvSizeGraph = new LineChart(title,
       * graphType.name(), xLabel, yLabel, BCSuiteConstants.DB_COL_MSG_SIZE,
       * BCSuiteConstants.DB_COL_PUB_TPUT_MSG_PER_SEC, strToken, GraphTypes.PVSIZE); return pvSizeGraph;
       */
      case PVNUM:
         xLabel = "Number of Publishers";
         yLabel = "Throughput (Msg/sec)";
         title = "Publisher throughput with varying number of publishers ";
         LineChart pvNumGraph = new LineChart(title, graphType.name(), xLabel, yLabel,
                  BCSuiteConstants.DB_COL_NUM_PUBLISHERS, BCSuiteConstants.DB_COL_PUB_TPUT_MSG_PER_SEC, strToken,
                  GraphTypes.PVNUM);
         return pvNumGraph;
      case PVSUB:
         xLabel = "Number of Subscribers";
         yLabel = "Throughput (Msg/sec)";
         title = "Publisher throughput with varying number of subscribers ";
         LineChart pvSumGraph = new LineChart(title, graphType.name(), xLabel, yLabel,
                  BCSuiteConstants.DB_COL_NUM_SUBSCRIBERS, BCSuiteConstants.DB_COL_PUB_TPUT_MSG_PER_SEC, strToken,
                  GraphTypes.PVSUB);
         return pvSumGraph;

      case SVSIZE:
         xLabel = "Message Size (bytes)";
         yLabel = "Throughput (Msg/sec)";
         title = "Subscriber Throughput with varying message size ";
         LineChart svSumGraph = new LineChart(title, graphType.name(), xLabel, yLabel,
                  BCSuiteConstants.DB_COL_MSG_SIZE, BCSuiteConstants.DB_COL_SUB_TPUT_MSG_PER_SEC, strToken,
                  GraphTypes.SVSIZE);
         return svSumGraph;

      case SVNUM:
         xLabel = "Number of Subscribers";
         yLabel = "Throughput (Msg/sec)";
         title = "Subscriber throughput with varying number of subscribers ";
         LineChart svNumGraph = new LineChart(title, graphType.name(), xLabel, yLabel,
                  BCSuiteConstants.DB_COL_NUM_SUBSCRIBERS, BCSuiteConstants.DB_COL_SUB_TPUT_MSG_PER_SEC, strToken,
                  GraphTypes.SVNUM);
         return svNumGraph;

      case SVPUB:
         xLabel = "Number of Subscribers";
         yLabel = "Throughput (Msg/sec)";
         title = "Subscriber throughput with varying number of publishers";
         LineChart svPubGraph = new LineChart(title, graphType.name(), xLabel, yLabel,
                  BCSuiteConstants.DB_COL_NUM_PUBLISHERS, BCSuiteConstants.DB_COL_SUB_TPUT_MSG_PER_SEC, strToken,
                  GraphTypes.SVPUB);
         return svPubGraph;

      case MSGCNT:
         title = "Total Messages Sent and Received ";
         PieChart peiChart = new PieChart(title, graphType.name(), strToken, GraphTypes.MSGCNT);
         return peiChart;             

      case LTNCNT:
         title = "Latency per message ";
         xLabel = "Message Samples";
         yLabel = "Latency (in microseconds)";
         ScatterChart scatterChart = new ScatterChart(title, graphType.name(), xLabel, yLabel, null,
                  BCSuiteConstants.DB_COL_LATENCY_IN_MS, strToken, GraphTypes.LTNCNT);
         return scatterChart;

      case LTNPER:
         title = "Latency as a percentile of dataset";
         xLabel = "% of Sample";
         yLabel = "Latency (in microseconds)";
         LatencyPercentileChart ltncyPerScatterChart = new LatencyPercentileChart(title, graphType.name(), xLabel,
                  yLabel, null, BCSuiteConstants.DB_COL_LATENCY_IN_MS, strToken, GraphTypes.LTNPER);
         return ltncyPerScatterChart;
      default:
         System.out.println("Graph Type [" + graphType + "] not supported");
         return null;
      }
   }

}
