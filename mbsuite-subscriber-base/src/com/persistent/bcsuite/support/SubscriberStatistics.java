package com.persistent.bcsuite.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubscriberStatistics {
   private Date processStartTime = new Date(Long.MAX_VALUE);
   private Date processEndTime;
   private List<SubscriberDetail> data;

   private int totalMessagesRecd = 0;
   private long totalBytesRecd = 0;

   public int getTotalMessagesRecd() {
      return totalMessagesRecd;
   }

   public void setTotalMessagesRecd(int totalMessagesRecd) {
      this.totalMessagesRecd = totalMessagesRecd;
   }

   public long getTotalBytesRecd() {
      return totalBytesRecd;
   }

   public void setTotalBytesRecd(long totalBytesRecd) {
      this.totalBytesRecd = totalBytesRecd;
   }

   public SubscriberStatistics(int size) {
      data = new ArrayList<SubscriberDetail>(size);
   }

   public SubscriberStatistics() {

   }

   public void addData(String messageId, Date recdOn, int messageSize,long latency) {
      totalMessagesRecd++;
      totalBytesRecd += messageSize;

      if (processStartTime.getTime()== Long.MAX_VALUE)
         processStartTime = recdOn;

      processEndTime = recdOn;

/*      if (data == null)
         return;

      data.add(new SubscriberDetail(latency));*/
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

   public List<SubscriberDetail> getData() {
      return data;
   }
}
