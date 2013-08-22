package com.persistent.bcsuite.charts.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.persistent.bcsuite.charts.constants.BCSuiteConstants;

/**
 * Utilities class for ReportGenerator. Contains methods for properties file
 * loading, copying files, etc.
 * 
 */
public class ReportUtils {

	private final static Logger logger = Logger.getLogger(ReportUtils.class);

	/**
	 * Load the DBURL parameter from XML configuration file.
	 * 
	 * @param xmlFileName
	 * @return - map containing the DB URL
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Map<String, String> loadXMLConfig(String xmlFileName)
			throws SAXException, IOException, ParserConfigurationException {
		Map<String, String> pubSetMap = new HashMap<String, String>();
		InputStream is = ClassLoader.getSystemResourceAsStream(xmlFileName);
		DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(is);
		NodeList nodeLst = doc
				.getElementsByTagName(BCSuiteConstants.PROP_DB_URL);
		Node node = nodeLst.item(0);
		Element eElement = (Element) node;
		String strDBURL = eElement.getTextContent();
		pubSetMap.put(BCSuiteConstants.PROP_DB_URL, strDBURL);
		logger.info("Loaded the Configuration File " + xmlFileName);

		return pubSetMap;
	}

	/**
	 * Loads the parameters from a properties file.
	 * 
	 * @param configFileName
	 * @return - map containing all the properties
	 * @throws IOException
	 */
	public static Map<String, String> loadConfig(String configFileName)
			throws IOException {
		ReportUtils reportUtils=new ReportUtils();
		Properties properties = new Properties();
		HashMap<String, String> propMap = new HashMap<String, String>();
		InputStream is = reportUtils.getClass().getClassLoader().getSystemResourceAsStream(configFileName);
		properties.load(is);
		Set<Object> propKeys = properties.keySet();
		for (Object key : propKeys) {
			propMap.put((String) key, (String) properties.get(key));
		}
		return propMap;
	}

	/**
	 * Splits the given string on the split symbol specified.
	 * 
	 * @param strSource
	 * @param splitSymbol
	 * @return - array of strings
	 */
	public static String[] strSplit(String strSource, String splitSymbol) {
		if (!strSource.isEmpty() && !splitSymbol.isEmpty()) {
			String[] strArr = strSource.split(splitSymbol);
			return strArr;
		}
		return null;
	}

	/**
	 * Copies all the files from the source folder to the destination folder.
	 * 
	 * @param sourceFolder
	 * @param destinationFolder
	 * @throws IOException
	 */
	public static void fileCopy(String sourceFolder, String destinationFolder)
			throws IOException {
		File source = new File(sourceFolder);
		FileFilter filter = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            if(pathname.isFile())
            {
               if(pathname.getName().indexOf("jpg") != -1 || pathname.getName().indexOf("jpeg") != -1||pathname.getName().indexOf("png") != -1)
                  return true;
            }
               
               return false;
         }
      };
		File[] files = source.listFiles(filter);
		for (File file : files) {
			FileChannel fIn = new FileInputStream(file).getChannel();
			FileChannel fOut = new FileOutputStream(new File(destinationFolder
					+ file.getName())).getChannel();
			fIn.transferTo(0, fIn.size(), fOut);
		}
	}
	
	

	/**
	 * Converts a double array to a Double array.
	 * 
	 * @param dbleArray
	 * @return - Double array of values
	 */
	public static Double[][] convertdoubleToDouble(double[][] dbleArray) {
		Double[][] dblArrayToReturn = new Double[dbleArray.length][2];
		if (dbleArray != null) {
			for (int i = 0; i < dbleArray.length; i++) {
				int j = 0;
				dblArrayToReturn[i][j] = dbleArray[i][j];
				dblArrayToReturn[i][j + 1] = dbleArray[i][j + 1];
			}
		}
		return dblArrayToReturn;
	}

	/**
	 * For a given array returns the values for domain (x) or range(y) axis as
	 * specified.
	 * 
	 * @param axisName
	 *            - the name of the axis - x or y
	 * @param values
	 *            - the array containing the x/y values data
	 * @return - array of values for range/domain axis
	 */
	public static double[] getAxisValueSet(String axisName, double[][] values) {
		double[] axisValueSet = new double[values.length];
		if (axisName.equals(BCSuiteConstants.XAXIS)) {
			for (int i = 0; i < values.length; i++) {
				int j = 0;
				axisValueSet[i] = values[i][j];
			}
		}
		if (axisName.equals(BCSuiteConstants.YAXIS)) {
			for (int i = 0; i < values.length; i++) {
				int j = 1;
				axisValueSet[i] = values[i][j];
			}
		}
		return axisValueSet;
	}

	/**
	 * Calculates the percentile values for a given set of values.
	 * 
	 * @param distribution
	 * @return - array of values and percentile values.
	 */
	public static double[][] calculatePercentile(double[] distribution) {
		double[][] result = new double[100][100];
		Percentile pecentile = new Percentile();
		pecentile.setData(distribution);
		for (int i = 1; i < 100; i++) {
			int j = 0;
			result[i - 1][j] = i - 1;
			result[i - 1][j + 1] = pecentile.evaluate(i);
		}
		return result;
	}

	/**
	 * Returns a file handle for the given file name. In case the directories
	 * specified in the file path do not exist, creates them.
	 * 
	 * @param filePath
	 * @return - file object for given file path.
	 * @throws IOException
	 */
	public static File getFile(String filePath) throws IOException {
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		file.createNewFile();
		return file;
	}

	/**
	 * Method to load the machine graphs based on the params in properties file.
	 * 
	 * @param strToken
	 *            - the database token name
	 * @return - list of graphs to be loaded.
	 * @throws IOException
	 */
	public static List<String> loadMachineStatGraphs(String strToken)
			throws IOException {
		Map<String, String> configMap = loadConfig(BCSuiteConstants.COLLECTOR_CONFIG_FILE);
		// based on the token name for the graphs include the image files for
		// the CPU,RAM and network stats
		// REad the machine stat graph types to be added to the report
		ArrayList<String> machineStatGraphNames = new ArrayList<String>();
		String strMachineGraphs = configMap
				.get(BCSuiteConstants.PROP_MACHINE_GRAPHS);
		if(strMachineGraphs != null)
		{
   		String[] arrMachineGraphs = ReportUtils.strSplit(strMachineGraphs, ",");
   		if (arrMachineGraphs != null) {
   			for (String strMchneGraph : arrMachineGraphs) {
   				if (BCSuiteConstants.CPU.equals(strMchneGraph)) {
   					machineStatGraphNames
   							.add(BCSuiteConstants.CPU_USAGE_GRAPH_1);
   					machineStatGraphNames
   							.add(BCSuiteConstants.CPU_USAGE_GRAPH_ALL);
   				}
   				if (BCSuiteConstants.NETWORK.equals(strMchneGraph)) {
   					machineStatGraphNames
   							.add(BCSuiteConstants.NETWORK_USAGE_GRAPH_ETH1);
   					machineStatGraphNames
   							.add(BCSuiteConstants.NETWORK_USAGE_GRAPH_ETH2);
   				}
   				if (BCSuiteConstants.MEMORY.equals(strMchneGraph)) {
   					machineStatGraphNames
   							.add(BCSuiteConstants.MEMORY_USAGE_GRAPH);
   				}
   			}
   			
   			try{
   			ReportUtils.fileCopy(BCSuiteConstants.FOLDER_PATH_KSAR_IMAGES
   					+ strToken + BCSuiteConstants.FOLDER_PATH_IMAGE,
   					BCSuiteConstants.FOLDER_PATH_BCSUITE + strToken
   							+ BCSuiteConstants.FOLDER_PATH_IMAGE);
   			}catch(Exception e)
   			{
   			   System.out.println("No machine stat graphs to copy");
   			}
   		}
		}
		return machineStatGraphNames;
	}
}
