package com.persistent.bcsuite.charts.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.utils.MySqlDAO;

public class GenericLineChart implements Graph {
   private double[][] tableVal;
   private double maxValue;
   private MySqlDAO dao;
   private String reportType;
   private String token;
   private String title;
   private String xLabel;
   private String yLabel;
   private String fileName;
   private static Random rand = new Random();
   
   public GenericLineChart(String rt,String t)
   {
      this.reportType = rt;
      this.token = t;
   }
   
   @Override
   public void plot() throws IOException, Exception {
      XYDataset dataset = createDataset();
      
      JFreeChart chart = ChartFactory.createXYLineChart(this.title,
            this.xLabel, this.yLabel, dataset, PlotOrientation.VERTICAL,
            true, true, false);
      
      if(maxValue <100)
      {
         XYPlot plot = chart.getXYPlot();
         NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setRange(0, 100);
      }
      XYItemRenderer renderer = chart.getXYPlot().getRenderer();

      chart.setAntiAlias(true);
      chart.setBorderVisible(true);
      
      StandardChartTheme theme = (StandardChartTheme)org.jfree.chart.StandardChartTheme.createJFreeTheme();
      String fontName = "Lucida Sans";
      theme.setTitlePaint( Color.BLACK);
      theme.setExtraLargeFont( new Font(fontName,Font.PLAIN, 16) ); //title
      theme.setLargeFont( new Font(fontName,Font.BOLD, 15)); //axis-title
      theme.setRegularFont( new Font(fontName,Font.PLAIN, 11));
      theme.setRangeGridlinePaint( Color.decode("#C0C0C0"));
      theme.setPlotBackgroundPaint( getPlotBackgroundPaint());
      theme.setChartBackgroundPaint( Color.white );
      theme.setGridBandPaint( Color.white );
      theme.setAxisOffset( new RectangleInsets(0,0,0,0) );
      theme.setBarPainter(new StandardBarPainter());
      theme.setAxisLabelPaint( Color.decode("#666666")  );
      theme.apply(chart);
      
      chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(3.0f));
      chart.getXYPlot().getRenderer().setSeriesPaint(0,Color.CYAN);
      
      ChartUtilities.saveChartAsPNG(getFile(), chart,
            BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);
   }

   private Paint getPlotBackgroundPaint()
   {
      int r = rand.nextInt(3) + rand.nextInt(4) + rand.nextInt(3);
      
      switch (r) {
      case 1:
         return Color.WHITE;
      case 2:
         return Color.BLACK;
      case 3:
         return Color.DARK_GRAY;
      case 4 :
         return Color.decode("#2C3539");
      case 5 :
         return Color.decode("#0000A0");
      case 6 :
         return Color.decode("#254117");
      case 7 :
         return Color.decode("#493D26");
      case 8 :
         return Color.decode("#9F000F");
      case 9 :
         return Color.decode("#571B7E");
      case 10 :
         return Color.BLUE;

      default:
         return Color.DARK_GRAY;
      }
      
   }
   
   private XYSeriesCollection createDataset() throws IOException,
   ClassNotFoundException, SQLException {
         MySqlDAO dao = new MySqlDAO();
         dao.init();
         Map<String,Object> data = dao.getGenericSummaryValueSet(reportType,token);
         title = (String)data.get("title");
         xLabel = (String)data.get("xLabel");
         yLabel = (String)data.get("yLabel");
         tableVal = (double[][])data.get("data");
         maxValue=(Double)data.get("maxValue");
         
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
      strBud.append("<img src=\"" + token
               + BCSuiteConstants.FOLDER_PATH_IMAGE + this.reportType+".png"
            + "\" alt=\"graph\"> ");
      strBud.append(BCSuiteConstants.TD_CLOSE);

      return strBud.toString();
   }
   
   /**
    * Returns the file object to which the graph file is to be written.
    * 
    * @return - file object
    * @throws IOException
    */
   public File getFile() throws IOException {
      fileName = BCSuiteConstants.FOLDER_PATH_BCSUITE + token
      + BCSuiteConstants.FOLDER_PATH_IMAGE + this.reportType+".png";
      File file = new File(fileName);
      file.getParentFile().mkdirs();
      file.createNewFile();
      return file;
   }

}
