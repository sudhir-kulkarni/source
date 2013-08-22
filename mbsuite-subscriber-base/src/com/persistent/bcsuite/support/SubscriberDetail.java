package com.persistent.bcsuite.support;

import java.util.Date;

public class SubscriberDetail {
   private String messageId;
   private Date recdOn;
   private int messageSize;
   private long latencyInNanoSeconds;

   public SubscriberDetail(String messageId, Date recdOn, int messageSize) {
      super();
      this.messageId = messageId;
      this.recdOn = recdOn;
      this.messageSize = messageSize;
   }

   public SubscriberDetail(long l) {
      this.latencyInNanoSeconds = l;
   }
   
   public String getMessageId() {
      return messageId;
   }

   public Date getRecdOn() {
      return recdOn;
   }

   public int getMessageSize() {
      return messageSize;
   }

   public long getLatencyInNanoSeconds() {
      return latencyInNanoSeconds;
   }
   
   
}
