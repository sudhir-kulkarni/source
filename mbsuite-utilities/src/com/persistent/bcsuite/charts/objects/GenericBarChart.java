package com.persistent.bcsuite.charts.objects;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.utils.MySqlDAO;

public class GenericBarChart implements Graph {

      private double[][] tableVal;
      private String reportType;
      private String token;
      private String title;
      private String xLabel;
      private String yLabel;
      private String fileName;

   public GenericBarChart(String rt,String t)
   {
      this.reportType = rt;
      this.token = t;
   }
   
   @Override
   public void plot() throws IOException, Exception {
      DefaultCategoryDataset dataset = createDataset();
      JFreeChart chart = ChartFactory.createBarChart(
               title, xLabel,yLabel,
               dataset, PlotOrientation.VERTICAL, false, true, false);
      ChartUtilities.saveChartAsPNG(getFile(), chart,
            BCSuiteConstants.CHART_WIDTH, BCSuiteConstants.CHART_HEIGHT);
   }
   
   private DefaultCategoryDataset createDataset() throws IOException,
   ClassNotFoundException, SQLException {
         MySqlDAO dao = new MySqlDAO();
         dao.init();
         Map<String,Object> data = dao.getGenericSummaryValueSet(reportType,token);
         title = (String)data.get("title");
         xLabel = (String)data.get("xLabel");
         yLabel = (String)data.get("yLabel");
         tableVal = (double[][])data.get("data");
         
        DefaultCategoryDataset d = new DefaultCategoryDataset();
         
         for (int i = 0; i < tableVal.length; i++) {
            int j = 0;
            d.setValue(tableVal[i][j++],yLabel,String.valueOf(tableVal[i][j]));
         }        
         dao.cleanUp();
         return d;
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

}
