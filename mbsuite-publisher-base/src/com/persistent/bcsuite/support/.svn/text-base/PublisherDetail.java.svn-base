package com.persistent.bcsuite.support;

import java.util.Date;

/**
 * This is a data holder used to hold the detail information regarding the message including the message id etc. This is
 * later used for the latency reporting.
 * 
 * 
 */
public class PublisherDetail {
   private String messageId;
   private Date sentOn;
   private int messageType;
   private int messageSizeinBytes;
   private long latencyInNanos;

   public Date getSentOn() {
      return sentOn;
   }

   public int getMessageType() {
      return messageType;
   }

   public String getMessageId() {
      return messageId;
   }

   public void setMessageId(String messageId) {
      this.messageId = messageId;
   }

   public int getMessageSizeinBytes() {
      return messageSizeinBytes;
   }

   public void setMessageSizeinBytes(int messageSizeinBytes) {
      this.messageSizeinBytes = messageSizeinBytes;
   }

   public PublisherDetail(String messageId, Date sentOn, int messageType, int messageSize) {
      super();
      this.messageId = messageId;
      this.sentOn = sentOn;
      this.messageType = messageType;
      this.messageSizeinBytes = messageSize;
   }
   
   
   
   public long getLatencyInNanos() {
      return latencyInNanos;
   }

   public void setLatencyInNanos(long latencyInNanos) {
      this.latencyInNanos = latencyInNanos;
   }

   public PublisherDetail(String messageId, long l) {
      super();
      this.messageId = messageId;
      this.latencyInNanos = l;
   }
}