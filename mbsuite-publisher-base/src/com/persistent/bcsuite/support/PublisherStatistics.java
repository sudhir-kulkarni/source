package com.persistent.bcsuite.support;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is a data holder to hold the metrics captured during the test run.
 * 
 */
public class PublisherStatistics {
   private Date processStartTime;
   private Date processEndTime;
   private int totalMessagesSent = 0;
   private long totalBytesSent = 0;

   private List<PublisherDetail> data;

   public PublisherStatistics(int totalMessages) {
      data = new ArrayList<PublisherDetail>(totalMessages);
   }

   public PublisherStatistics() {
   }

   public Date getProcessStartTime() {
      return processStartTime;
   }

   public void setProcessStartTime(Date processStartTime) {
      this.processStartTime = processStartTime;
   }

   public Date getProcessEndTime() {
      return processEndTime;
   }

   public void setProcessEndTime(Date processEndTime) {
      this.processEndTime = processEndTime;
   }

   public void addData(String messageId, Date sentOn, int messageType, int messageSizeInBytes) {
      totalMessagesSent++;
      totalBytesSent += messageSizeInBytes;
      if (data == null)
         throw new RuntimeException("Cannot capture detail data, the dumpDetails configuration property is false");

/*      PublisherDetail d = new PublisherDetail(messageId, sentOn, messageType, messageSizeInBytes);
      data.add(d);*/
   }

   public void addData(int messageSizeInBytes) {
      totalMessagesSent++;
      totalBytesSent += messageSizeInBytes;
   }
   
   public void addLatency(String messageId, long latencyInNanos) {
      if (data == null)
      {
         System.out.println("Cannot store latency info, dumpDetails property is false");
         return;
      }
      
      PublisherDetail d = new PublisherDetail(messageId,latencyInNanos);
      data.add(d);
   }

   public List<PublisherDetail> getDetailData() {
      return data;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyy hh:mm:ss SSS");
      for (PublisherDetail d : data) {
         sb.append(d.getMessageId()).append(",").append(d.getMessageType()).append(",")
                  .append(sdf.format(d.getSentOn())).append("\n");
      }
      return sb.toString();
   }

   public int getTotalMessagesSent() {
      return totalMessagesSent;
   }

   public void setTotalMessagesSent(int totalMessagesSent) {
      this.totalMessagesSent = totalMessagesSent;
   }

   public long getTotalBytesSent() {
      return totalBytesSent;
   }

   public void setTotalBytesSent(long totalBytesSent) {
      this.totalBytesSent = totalBytesSent;
   }
}