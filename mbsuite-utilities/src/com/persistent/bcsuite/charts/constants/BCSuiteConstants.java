package com.persistent.bcsuite.charts.constants;

/**
 * Constants required for the ReportGeneration Utility.
 * 
 * 
 */
public class BCSuiteConstants {

	public static final String Publisher_Througput_by_Varying_Size = "Vary Message Size";
	public static final String Publisher_Througput_by_Varying_Number = "Vary Number of Publishers";
	public static final String XAXIS = "x";
	public static final String YAXIS = "y";
	public final static String DOT = ".";
	public final static String FORWARD_SLASH = "/";
	public final static String COLON = ":";
	public final static String SEMI_COLON = ";";
	public final static String COMMA = ",";
	public static final String FILE_TYPE_EXTN_PNG = ".png";
	public static final String FILE_TYPE_EXTN_HTML = ".html";
	public static final String REPORT_FILE_NAME = "report.html";
	public static final String FOLDER_NAME_EXPORT = "export";
	public static final String FOLDER_NAME_IMAGE = "image";
	public static final String FOLDER_PATH_KSAR_IMAGES = "../../export/ksar/";
	public static final String FOLDER_PATH_EXPORT = "../../export/";
	public static final String FOLDER_PATH_BCSUITE = "../../export/bcsuite/";
	public static final String FOLDER_PATH_IMAGE = "/images/";
	public static final String PUBLISHER_CONFIG_FILE = "publisher-config.xml";
	public static final String COLLECTOR_CONFIG_FILE = "properties/collector.properties";
	public static final String GRAPH_CONFIG_FILE = "graph.properties";

	public static final String PROP_DB_URL = "db-url";
	public static final String PROP_MACHINE_GRAPHS = "machine_graphs";

	// Chart constants
	public static final int CHART_WIDTH = 600;
	public static final int CHART_HEIGHT = 270;

	// HTML Constants
	public static final String TABLE_REPORT_OPEN = "<table id=\"report\">";
	public static final String TABLE_REPORT_MAIN_OPEN = "<table border=\"1\" id=\"reportmain\">";
	public static final String TABLE_CLOSE = "</table>";
	public static final String TH_OPEN = "<th>";
	public static final String TH_CLOSE = "</th>";
	public static final String TR_OPEN = "<tr>";
	public static final String TR_CLOSE = "</tr>";
	public static final String TD_OPEN = "<td style=\"padding-left: 0px; cursor: pointer; vertical-align: top;\">";
	public static final String TD_CLOSE = "</td>";

	// Machine status graph tokens and names
	public static final String CPU = "cpu";
	public static final String MEMORY = "memory";
	public static final String NETWORK = "network";
	public static final String CPU_USAGE_GRAPH_1 = "export_img.jpeg_0-cpu.jpg";
	public static String CPU_USAGE_GRAPH_ALL = "export_img.jpeg_all-cpu.jpg";
	public static final String MEMORY_USAGE_GRAPH = "export_img.jpeg_LinuxkbmemSar.jpg";
	public static final String NETWORK_USAGE_GRAPH_ETH1 = "export_img.jpeg_eth6-if1.jpg";
	public static final String NETWORK_USAGE_GRAPH_ETH2 = "export_img.jpeg_eth6-if2.jpg";

	// Database table constants
	public static final String DB_COL_NUM_PUBLISHERS = "num_publishers";
	public static final String DB_COL_NUM_SUBSCRIBERS = "num_subscribers";
	public static final String DB_COL_MSG_SIZE = "message_size";
	public static final String DB_COL_PUB_TPUT_MSG_PER_SEC = "tput_msg_per_sec";
	public static final String DB_COL_SUB_TPUT_MSG_PER_SEC = "subs_tput_msg_per_sec";
	public static final String DB_COL_MSG_SENT = "Sent (%)";
	public static final String DB_COL_MSG_LOST = "Lost (%)";
	public static final String DB_COL_MSG_NO = "message_no";
	public static final String DB_COL_LATENCY_IN_MS = "latency_in_nanos";
}
