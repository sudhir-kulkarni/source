package com.persistent.bcsuite.charts.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;
import com.persistent.bcsuite.charts.factory.GraphGeneratorFactory;
import com.persistent.bcsuite.charts.graphs.GraphPlotter;
import com.persistent.bcsuite.charts.graphs.GraphTypes;
import com.persistent.bcsuite.charts.objects.Graph;
import com.persistent.bcsuite.charts.utils.ReportUtils;

/**
 * This class generates the HTML report from the statistics collected in the
 * database. It is also responsible for creating the required graphs by invoking
 * necessary classes.
 * 
 * 
 */
public class ReportGenerator {

	private final static Logger logger = Logger
			.getLogger(ReportGenerator.class);
	private static Connection connection;

	public static void main(String[] args) throws SQLException, IOException,
			ClassNotFoundException {
		// Get the graph type and token from user
		if (args.length == 0) {
			System.out
					.println("Please enter comma seperated list of graph type(s) and token separated by colon.\n E.g<PVSIZE>:<msg-vary-size>,<PVNUM>:<varying-pub-size>");
			System.out.println("Different Graph Types : ");
			System.out
					.println("PVSIZE  : Publisher Throughput with varying message size ");
			System.out
					.println("PVNUM   : Publisher Throughput with varying number of publishers ");
			System.out
					.println("PVSUB   : Publisher Throughput with varying number of Subscribers ");
			System.out
					.println("SVSIZE  : Subscriber Throughput with varying message size ");
			System.out
					.println("SVNUM   : Subscriber Throughput with varying number of subscribers ");
			System.out
					.println("SVPUB   : Subscriber Throughput with varying number of Publishers ");
			System.out.println("MSGCNT  : Messages sent and received ");
			System.out.println("LTNCNT  : Latency for per message ");
			System.out
					.println("LTNPER  : Latency for each message as percentile ");
			return;
		}

		ReportGenerator repGen = new ReportGenerator();
		try {
			repGen.process(args[0].trim());
		} catch (Exception e) {
			logger.error("Exception in Processing Report", e);
		}
		repGen.cleanUp();

	}

	public void process(String strGraphTypesLst) throws Exception {
		GraphPlotter graphPlot = new GraphPlotter();
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		// For each type of graph request run the loop
		GraphGeneratorFactory graphFactory = GraphGeneratorFactory
				.getInstance();
		String[] arrGraphTypes = ReportUtils.strSplit(strGraphTypesLst,
				BCSuiteConstants.COMMA);
		for (int i = 0; i < arrGraphTypes.length; i++) {
			String strGraphTypeToke = arrGraphTypes[i].trim();
			// Split the graph type into token and graph type
			String[] arrGTypeToken = ReportUtils.strSplit(strGraphTypeToke,
					BCSuiteConstants.COLON);
			String strGraphType = arrGTypeToken[0].trim();
			String token = arrGTypeToken[1].trim();
			GraphTypes graphType = GraphTypes.fromString(strGraphType);
			if(graphType == null)
			{
			   graphs.add(graphFactory.getGraph(token,strGraphType));
			}
			else
			{
			   graphs.add(graphFactory.getGraph(graphType, token));
			}
		}
		graphPlot.plotGraph(graphs);
		generateHTMLReport(graphs, BCSuiteConstants.REPORT_FILE_NAME);
	}

	/**
	 * Method that generates the HTML report.
	 * 
	 * @param graphs
	 *            - list of graphs to be added to the report.
	 * @param reportFileName
	 *            - name of the report file
	 * @throws IOException
	 */
	public void generateHTMLReport(List<Graph> graphs, String reportFileName)
			throws IOException {
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(
				ReportUtils.getFile(BCSuiteConstants.FOLDER_PATH_BCSUITE
						+ reportFileName)));
		StringBuilder strBud = new StringBuilder();
		strBud.append("<html><head><title> REPORT </title></head>");
		strBud.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"reportstyle.css\">");
		strBud.append("<body>");

		strBud.append(BCSuiteConstants.TABLE_REPORT_MAIN_OPEN);
		strBud.append(BCSuiteConstants.TR_OPEN);
		for (Graph g : graphs) {
			strBud.append(BCSuiteConstants.TD_OPEN);// Main table td
			strBud.append(g.generateTable());
			strBud.append(BCSuiteConstants.TD_CLOSE);// Main table td close
		}
		strBud.append(BCSuiteConstants.TR_CLOSE);
		strBud.append(BCSuiteConstants.TABLE_CLOSE);

		strBud.append("</body></html>");
		bw.write(strBud.toString());
		bw.close();
		
		//Copy the stylesheet also
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("reportstyle.css");
		File dest = new File(BCSuiteConstants.FOLDER_PATH_BCSUITE + "reportstyle.css");
		FileOutputStream out = new FileOutputStream(dest);
		try
	    {
	        try
	        {
	            final byte[] buffer = new byte[1024];
	            int n;
	            while ((n = in.read(buffer)) != -1)
	                out.write(buffer, 0, n);
	        }
	        finally
	        {
	            out.close();
	        }
	    }
	    finally
	    {
	        in.close();
	    }
		
		
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
