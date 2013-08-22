package com.persistent.bcsuite.dataStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Scanner;
import org.apache.log4j.Logger;

/**
 * This class is a utility class used to persist the statistics data to the database or file.
 */
public class Loader {
	private static Properties prop = new Properties();
	private static InputStream propertiesFile;
	private static final Logger logger = Logger.getLogger(Loader.class
			.getName());
	private static String separator = null;
	private static String dburl;
	private static String dbcol;
	private static String dbtbl;
	private static String ss = null;
	private static String line_separator = null;
	private static String skipLines = null;
	private static Scanner scanner = null;
	private static String dumpDestination;

	
	/**
	 * This method accepts DB URL as it's parameter ant returns the connection . 
	 */
	
	private static Connection connect(String targetDB) {
		Connection conn;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(targetDB);
			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
			conn = null;
		}

		return conn;
	}

	public static void main(String[] args) {

		if (args.length == 0 || args.length > 2)
			logger.info("Input sequence is not correct\n valid sequence is    E.g<./filename>:<properties file_name> <token>");
		try {
			Loader loader = new Loader();
			propertiesFile = loader
					.getClass()
					.getClassLoader()
					.getResourceAsStream(
							"properties/" + args[0] + ".properties");
			prop.load(propertiesFile);

			// Get the database-URL & dumpDestination from properties file.
			
			InputStream commonSettingsInputStream = loader.getClass()
					.getClassLoader()
					.getResourceAsStream("common-settings.properties");
			Properties commonSettings = new Properties();
			commonSettings.load(commonSettingsInputStream);
			dburl = commonSettings.getProperty("db-url");
			dumpDestination = commonSettings.getProperty("dump-destination");

			// Get the database-column,database-table-name,skiplines & separator from properties file
			
			dbcol = prop.getProperty("db-coloumn");
			dbtbl = prop.getProperty("db-table");
			skipLines = prop.getProperty("skipLines");
			ss = prop.getProperty("separator");
			if (ss == null || ss.trim().length() == 0)
				throw new RuntimeException("Invalid Separator");
			else if (ss.equalsIgnoreCase("SPACE")) {
				separator = "\\s+";
				line_separator = " ";
			} else if (ss.equalsIgnoreCase("TAB")) {
				separator = "\\t+";
				line_separator = "\t";
			} else if (ss.equalsIgnoreCase("colon")) {
				separator = ":+";
				line_separator = ":";
			} else if (ss.equalsIgnoreCase("comma")) {
				separator = ",+";
				line_separator = ",";
			} else
				separator = ss;

			String[] spliit_on_comma = dbcol.split(",");
			
			// Check if dump-destination is db or file
			
			if ("db".equalsIgnoreCase(dumpDestination)
					|| dumpDestination == null
					|| dumpDestination.trim().length() == 0) {
				loader.importDataInDB(args[1], spliit_on_comma, separator,
						line_separator, args[0]);
			} else {
				loader.importDataInCSV(args[0], args[1], spliit_on_comma,
						separator, line_separator);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * This method dumps captured data into DB.
	 */

	private void importDataInDB(String token, String[] spliit_on_comma,
			String separator, String line_separator, String fileName) {
		InputStream txtFile = null;
		Connection conn = connect(dburl);
		txtFile = this.getClass().getClassLoader()
				.getResourceAsStream(fileName + ".txt");
		scanner = new Scanner(txtFile);
		
		try {

			StringBuffer query = new StringBuffer("insert into ");
			query.append(dbtbl).append("(");
			StringBuffer questionMarks = new StringBuffer("(");
			for (int i = 0; i < spliit_on_comma.length; i++) {
				String[] c = spliit_on_comma[i].split(":");
				query.append(c[0]);
				questionMarks.append("?");

				if (i != spliit_on_comma.length - 1) {
					query.append(",");
					questionMarks.append(",");
				} else {
					query.append(")");
					questionMarks.append(")");
				}

			}

			PreparedStatement dataEntry = conn.prepareStatement(query
					.toString() + " values " + questionMarks.toString());

			int lineNumber = 1;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int sl = Integer.parseInt(skipLines);
				if (lineNumber <= sl) {
					lineNumber++;
					continue;
				}

				try {
					line = line.trim();

					line = token + line_separator + line;
					String[] values = line.split(separator);
				
					for (int i = 1; i <= spliit_on_comma.length; i++) {
						String[] c = spliit_on_comma[i - 1].split(":");

						if (c[1].equalsIgnoreCase("String")) {
							dataEntry.setString(i, values[i - 1]);
						} else if (c[1].equalsIgnoreCase("int")) {
							dataEntry
									.setInt(i, Integer.parseInt(values[i - 1]));
						} else if (c[1].equalsIgnoreCase("float")) {
							dataEntry.setFloat(i,
									Float.parseFloat(values[i - 1]));
						} else if (c[1].equalsIgnoreCase("double")) {
							dataEntry.setDouble(i, Math.round(Double
									.parseDouble(values[i - 1])));
						} else if (c[1].equalsIgnoreCase("Long")) {
							dataEntry.setLong(i, Long.parseLong(values[i - 1]));
						} else if (c[1].equalsIgnoreCase("timestamp")) {
							dataEntry
									.setTimestamp(
											i,
											new Timestamp(Long
													.parseLong(values[i - 1])));
						} else if (c[1].equalsIgnoreCase("time")) {
							dataEntry.setTime(
									i,
									new Time(1000 * Long
											.parseLong(values[i - 1])));
						}

						else {
							throw new RuntimeException("Unsupproted Data Type");
						}

					}
				} catch (Exception e) {
					logger.error("Row could not be loaded. [" + line + "]");

				}
				int result = dataEntry.executeUpdate();

			}
		} catch (Exception e) {
			logger.error("could not insert record");
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				propertiesFile.close();
				scanner.close();
				txtFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * This method dumps captured data into .csv file.
	 */

	private void importDataInCSV(String fileName, String token,
			String[] spliit_on_comma, String separator, String line_separator) {
		// TODO Auto-generated method stub
		InputStream txtFile = null;
		txtFile = this.getClass().getClassLoader()
				.getResourceAsStream(fileName + ".txt");
		FileWriter fileWriter = null;
		File dir = null;
		File file = null;
		scanner = new Scanner(txtFile);

		// Get location of /csv dir 
		
		URL location = this.getClass().getResource("/csv");
		String exportPath = location.getPath();

		dir = new File(exportPath);
		if (!dir.exists()) {
			
			// if dir is not exist then create it
			
			dir.mkdir();
		}

		try {

			file = new File(exportPath + File.separator + fileName + ".csv");

			fileWriter = new FileWriter(file, true);

			if (file.length() == 0) {

				for (int i = 0; i < spliit_on_comma.length; i++) {
					String[] c = spliit_on_comma[i].split(":");
					fileWriter.append(c[0]);
					if (i != spliit_on_comma.length - 1) {
						fileWriter.append(",");
					}
				}
				fileWriter.append("\n");

			}

			int lineNumber = 1;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int sl = Integer.parseInt(skipLines);
				if (lineNumber <= sl) {
					lineNumber++;
					continue;
				}
				try {
					line = line.trim();
					line = token + line_separator + line;
					String[] values = line.split(separator);

					for (int i = 1; i <= values.length; i++) {
						fileWriter.append(values[i - 1]);
						if (i != values.length) {
							fileWriter.append(",");
						} else {
							fileWriter.append("\n");
						}
					}

				}

				catch (Exception e) {
					logger.error("Row could not be loaded. [" + line + "]");
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
			scanner.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}