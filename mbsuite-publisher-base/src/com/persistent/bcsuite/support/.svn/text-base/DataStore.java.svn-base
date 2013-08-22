package com.persistent.bcsuite.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * This class is a utility class used to persist the statistics data to the database.
 */
public class DataStore {
   private static final Logger logger = Logger.getLogger(DataStore.class);
   //private static final String DETAILS_INSERT_SQL = "insert into publisher_detail(token,iteration,sent_ts,message_id,message) values (?,?,?,?,?)";
   private static final String DETAILS_INSERT_SQL = "insert into publisher_detail(token,iteration,message_id,latency_in_nanos) values (?,?,?,?)";
   private static final String SUMMARY_INSERT_SQL = "insert into publisher_summary(token,iteration,start_date,end_date,num_publishers,message_size,total_messages_sent,tput_msg_per_sec,total_mb_sent,tput_mb_per_sec,attributes,test_group_name,task_name) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";

   public static void saveDetails(String targetDB, List<PublisherDetail> tdList, Map<String, String> params)
            throws SQLException {
      int batchCount = 0;
      Connection conn = getConnection(targetDB);
      if (conn == null)
         return;
      try {
         PreparedStatement psDetail = conn.prepareStatement(DETAILS_INSERT_SQL);
         for (PublisherDetail pd : tdList) {
            try {
               psDetail.setString(1, params.get("instancetoken"));
               psDetail.setInt(2, Integer.parseInt(params.get("runcounter")));
               psDetail.setString(3, pd.getMessageId());
               psDetail.setLong(4, pd.getLatencyInNanos());
               psDetail.addBatch();
               batchCount++;

               if (batchCount > 30000) {
                  logger.debug("30K reached...executing batch...");
                  psDetail.executeBatch();
                  psDetail.clearBatch();
                  batchCount = 0;
               }
            } catch (SQLException e) {
               e.printStackTrace();
            }
         }

         if (batchCount > 0 && psDetail != null) {
            logger.debug("Executing final batch for the publisher");
            try {
               psDetail.executeBatch();
               psDetail.clearBatch();
               batchCount = 0;
            } catch (SQLException e) {
               e.printStackTrace();
            }
         }
      } finally {
         conn.close();
      }
   }

   @SuppressWarnings("rawtypes")
   public static void saveSummary(String targetDB, Map<String, String> attributes, Map<String, Object> params)
            throws SQLException {
      Connection conn = getConnection(targetDB);
      if (conn == null)
         return;
      PreparedStatement ps = conn.prepareStatement(SUMMARY_INSERT_SQL);
      try {
         StringBuffer sbf = new StringBuffer();
         if (attributes != null && attributes.size() > 0) {
            for (Entry entry : attributes.entrySet()) {
               sbf.append(entry.getValue()).append(",");
            }
         }
         ps.setString(1, (String) params.get("instancetoken"));
         ps.setInt(2, (Integer) params.get("runcounter"));
         ps.setTimestamp(3, new java.sql.Timestamp((Long) params.get("processstarttime")));
         ps.setTimestamp(4, new java.sql.Timestamp((Long) params.get("processendtime")));
         ps.setInt(5, (Integer) params.get("totalpublisherthreads"));
         ps.setInt(6, (Integer) params.get("messagesizeinbytes"));
         ps.setInt(7, (Integer) params.get("totalmsgsent"));
         ps.setInt(8, ((Double) params.get("noofmsgpersec")).intValue());
         ps.setInt(9, ((Double) params.get("mbsent")).intValue());
         ps.setDouble(10, (Double) params.get("mbpersec"));
         ps.setString(11, sbf.toString());
         ps.setString(12, (String) params.get("testgroup"));
         ps.setString(13, (String) params.get("taskkey"));

         ps.execute();
      } finally {
         conn.close();
      }

   }

   private static Connection getConnection(String targetDB) {
      Connection conn = null;
      try {
         Class.forName("com.mysql.jdbc.Driver").newInstance();
         conn = DriverManager.getConnection(targetDB);
         conn.setAutoCommit(true);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return conn;
   }
}
