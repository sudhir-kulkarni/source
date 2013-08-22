package com.persistent.bcsuite.charts.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;

/**
 * Class for accessing the database and retrieving the values for given sql
 * queries.
 * 
 */
public class MySqlDAO {
	private final static Logger logger = Logger.getLogger(MySqlDAO.class);
	private static Connection connection;
	private PreparedStatement prepStatment;
	private ResultSet result;
	private static Map<String, String> configMap;
	private static Map<String, String> commonSettingsMap;
	private static Map<String,String> sqlQueryMap;

	public void init() throws IOException, ClassNotFoundException, SQLException {
		configMap = ReportUtils
				.loadConfig(BCSuiteConstants.COLLECTOR_CONFIG_FILE);
		
		sqlQueryMap = ReportUtils.loadConfig("properties/queries.properties");

		commonSettingsMap = ReportUtils
				.loadConfig("common-settings.properties");
				
		if (configMap == null || configMap.isEmpty()) {
			logger.error("Empty configuration file");
			return;
		}
		// Load the driver
		try {
			if (connection == null || connection.isClosed()) {
				// Log opening new database connection
				Class.forName("com.mysql.jdbc.Driver");
				// Create the data base connection
				String db_url = commonSettingsMap.get(BCSuiteConstants.PROP_DB_URL);
				// Connect to the database
				connection = DriverManager.getConnection(db_url);

			}
		} catch (ClassNotFoundException e) {
			logger.error("Error Loading Database driver : ", e);
			throw e;
		} catch (SQLException e) {
			logger.error("Exception accessing database : ", e);
			throw e;
		}
	}

	/**
	 * This method generates the report by collating the data using appropriate
	 * sql query based on Graph Type
	 * 
	 * @param strSQLQuery
	 *            - query for fetching the data
	 * @param token
	 *            - database token
	 * @return - list of values for plotting the graph
	 */
	public double[][] getSummaryValueSet(String strSQLQuery,
			String xAxisColName, String yAxisColName, String token) {
		try {
			logger.debug("Getting the Values for " + strSQLQuery);
			prepStatment = connection.prepareStatement(strSQLQuery);
			prepStatment.setString(1, token);
			result = prepStatment.executeQuery();
			
			if (result != null) {
				int count = getRowCount(result);
				double[][] values = new double[count][2];
				int i = 0;
				int index = 0;
				while (result.next()) {
					int j = 0;
					if (xAxisColName != null) {
						values[i][j] = result.getDouble(xAxisColName);
					} else {// Populate the x-axis values with the index value
						index++;
						values[i][j] = index;
					}
					values[i][j + 1] = result.getDouble(yAxisColName);
					i++;
				}
				result.close();
				return values;
			}

		} catch (SQLException e) {
			logger.error("Error retrieving the data from database ", e);
		}

		return null;

	}

	  public Map<String,Object> getGenericSummaryValueSet(String reportType,String token) 
	  {
	     String interfaceName1G = commonSettingsMap.get("1g.interface.name");
	     String interfaceName10G = commonSettingsMap.get("10g.interface.name");
	     Map<String,Object> dataSet = new HashMap<String,Object>();
	     String xLabel=null;
	     String yLabel=null;
	     String strSQLQueryToken = sqlQueryMap.get(reportType);
	     double maxValue=0;
	     String[] s = strSQLQueryToken.split("#");
	     
	     String strSQLQuery = s[0];
	     String reportTitle = null;
	     
	     if(s.length > 1)
	     reportTitle = s[1];
	     
	     
	     if(strSQLQuery == null || strSQLQuery.trim().length() ==0)
	     {
	        System.out.println("No query specified for the report type ["+reportType+"]");
	        return null;
	     }
	     
	     strSQLQuery = strSQLQuery.replaceAll("-1g.interface.name-", interfaceName1G);
	     strSQLQuery = strSQLQuery.replaceAll("-10g.interface.name-", interfaceName10G);
	     try {
	           logger.debug("Getting the Values for " + strSQLQuery);
	           prepStatment = connection.prepareStatement(strSQLQuery);
	           
	           if(strSQLQuery.indexOf("?") != -1)
	           prepStatment.setString(1, token);
	           
	           result = prepStatment.executeQuery();
	           if(result.getMetaData().getColumnCount() == 2)
	           {
	              xLabel = result.getMetaData().getColumnLabel(1);
	              yLabel = result.getMetaData().getColumnLabel(2);
	           }
	           else
	           {
	              xLabel = "Elapsed Time (secs)";
	              yLabel = result.getMetaData().getColumnLabel(1);
	           }
	           	           
	           if (result != null) {
	              int count = getRowCount(result);
	              double[][] values = new double[count][2];
	              int i = 0;
	              int index = 0;
	              while (result.next()) {           
	                 if (result.getMetaData().getColumnCount() == 2) {
	                    values[i][0] = result.getDouble(1);
	                    values[i][1] = result.getDouble(2);
	                 } else {// Only one column use that as y-Axis and populate the x-axis values with the index value
	                    index++;
	                    values[i][0] = index;
	                    values[i][1] = result.getDouble(1);
	                 }
	                 if(values[i][1] > maxValue){
	                    maxValue = values[i][1];}
	                 
	                 i++;
	              }
	              result.close();
	              dataSet.put("data", values);
	              dataSet.put("xLabel", xLabel);
	              dataSet.put("yLabel", yLabel);
	              dataSet.put("title", reportTitle);
	              dataSet.put("maxValue", maxValue);
	              return dataSet;
	           }
	        } catch (SQLException e) {
	           logger.error("Error retrieving the data from database ", e);
	        }

	        return dataSet;

	     }

	
	
	/**
	 * Get the data values for generating a pie chart from the database
	 * 
	 * @param strSQLQuery
	 *            - query for fetching the data
	 * @param token
	 *            - database token
	 * @return - list of values for plotting the pie chart
	 */
	public HashMap<String, Double> getPieValueSet(String strSQLQuery,
			String token) {
		HashMap<String, Double> pieValMap = new HashMap<String, Double>();

		try {
			logger.debug("Getting the Values for " + strSQLQuery);
			prepStatment = connection.prepareStatement(strSQLQuery);
			prepStatment.setString(1, token);
			result = prepStatment.executeQuery();
			if (result != null) {
				while (result.next()) {
					pieValMap.put(BCSuiteConstants.DB_COL_MSG_SENT,
							result.getDouble(1));
					pieValMap.put(BCSuiteConstants.DB_COL_MSG_LOST,
							result.getDouble(2));
				}
				result.close();
			}

		} catch (SQLException e) {
			logger.error("Error retrieving the data from database ", e);
		}

		return pieValMap;

	}

	private int getRowCount(ResultSet result) throws SQLException {
		result.last();
		int count = result.getRow();
		result.beforeFirst();
		return count;
	}

	/**
	 * Method to clean up after program completes. Closes the SQL data
	 * connections.
	 */
	public void cleanUp() {

		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error("Error closing database connection : ", e);
		}

	}
}
