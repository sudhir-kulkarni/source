package com.persistent.bcsuite.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class FileStore {

	private static final String DETAILS_COLUMN = "token,iteration,message_id,latency_in_nanos";
	private static final String SUMMARY_COLUMN = "token,iteration,start_date,end_date,num_publishers,message_size,total_messages_sent,tput_msg_per_sec,total_mb_sent,tput_mb_per_sec,attributes,test_group_name,task_name";

	public static void saveDetails(List<PublisherDetail> tdList,
			Map<String, String> params,URL exportURL) {

		String[] spliit_on_comma_for_details = DETAILS_COLUMN.split(",");
		FileWriter fileWrite = null;
		File dir = null;
		File file = null;
	    String exportPath = exportURL.getPath();
		dir = new File(exportPath);
		if (!dir.exists()) {
			// if dir is not exixt then create it
			dir.mkdir();
		}
	
		try {
		
			file=new File(exportPath+File.separator+"PublisherDetail.csv");

			fileWrite = new FileWriter(file, true);

			if (file.length() == 0) {

				for (int i = 0; i < spliit_on_comma_for_details.length; i++) {
					String[] c = spliit_on_comma_for_details[i].split(":");
					fileWrite.append(c[0]);
					if (i != spliit_on_comma_for_details.length - 1) {
						fileWrite.append(",");
					}
				}
				fileWrite.append("\n");

			}

			for (PublisherDetail pd : tdList) {

					fileWrite.append(params.get("instancetoken"));
					fileWrite.append(",");
					fileWrite.append(params.get("runcounter"));
					fileWrite.append(",");
					fileWrite.append(pd.getMessageId());
					fileWrite.append(",");
					fileWrite.append(String.valueOf(pd.getLatencyInNanos()));
					fileWrite.append(",");
					fileWrite.append("\n");								

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(fileWrite != null ) fileWrite.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static  void saveSummary(Map<String, String> attributes,
			Map<String, Object> params,URL exportURL) {
		String[] spliit_on_comma_for_summary = SUMMARY_COLUMN.split(",");
		FileWriter fileWriter = null;
		File dir = null;
		File file = null;		
	    String exportPath = exportURL.getPath(); 
			dir = new File(exportPath);
			if (!dir.exists()) {
				// if dir is not exixt then create it
				dir.mkdir();
			}
		
			try {
		
			file=new File(exportPath+File.separator+"PublisherSummary.csv");

			fileWriter = new FileWriter(file, true);

			if (file.length() == 0) {

				for (int i = 0; i < spliit_on_comma_for_summary.length; i++) {
					String[] c = spliit_on_comma_for_summary[i].split(":");
					fileWriter.append(c[0]);
					if (i != spliit_on_comma_for_summary.length - 1) {
						fileWriter.append(",");
					}
				}
				fileWriter.append("\n");

			}

			try {
				StringBuffer sbf = new StringBuffer();
				if (attributes != null && attributes.size() > 0) {
					for (Entry entry : attributes.entrySet()) {
						sbf.append(entry.getValue()).append(":");
					}
				}

				fileWriter.append(String.valueOf(params.get("instancetoken")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params.get("runcounter")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(new java.sql.Timestamp(
						(Long) params.get("processstarttime"))));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(new java.sql.Timestamp(
						(Long) params.get("processendtime"))));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params
						.get("totalpublisherthreads")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params
						.get("messagesizeinbytes")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params.get("totalmsgsent")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params.get("noofmsgpersec")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params.get("mbpersec")));
				fileWriter.append(",");
				fileWriter.append(sbf.toString());
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params.get("testgroup")));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(params.get("taskkey")));
				fileWriter.append("\n");

			} finally {
				fileWriter.close();
			}
		}

		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

}
