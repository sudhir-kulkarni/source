package com.persistent.bcsuite.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.base.PublisherBase;
import com.persistent.bcsuite.beans.PublisherGroup;
import com.persistent.bcsuite.beans.PublisherSettings;

public abstract class BaseGenerator {
   private static final Logger logger = Logger.getLogger(BaseGenerator.class.getName());
   protected PublisherSettings publisherSettings;
   protected String key;
   protected String testgroup;
   protected int repeatCounter;
   protected String instanceToken = null;
   PublisherGroup pg = null;
   protected String dbUrl=null;
   protected static String dumpDestination; 
   public BaseGenerator(String key, String testgroup, String token,int rc) {
      this.key = key;
      this.testgroup = testgroup;
      this.instanceToken = token;
      this.repeatCounter = rc;
      
      init();
   }
   
   private void init() {
      try {
         InputStream is = this.getClass().getClassLoader().getResourceAsStream("publisher-config.xml");
         JAXBContext jaxbContext = JAXBContext.newInstance(PublisherSettings.class);

         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
         publisherSettings = (PublisherSettings) jaxbUnmarshaller.unmarshal(is);
         logger.info("Sucessfully Loaded Publisher Configuration for the test run");
         logger.info("Token is [" + instanceToken + "]");
		 
		 InputStream commonSettingsInputStream = this.getClass().getClassLoader().getResourceAsStream("common-settings.properties");
		 Properties commonSettings = new Properties();
		 commonSettings.load(commonSettingsInputStream);
		 dbUrl = commonSettings.getProperty("db-url");
		 dumpDestination = commonSettings.getProperty("dump-destination");
		 logger.info("Sucessfully Loaded common-settings properties");
      } catch (JAXBException e) {
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }   
   
   
   protected byte[] getByteArray(int length) {
      byte[] b = new byte[length];
      for (int i = 0; i < b.length; i++) {
         b[i] = 'x';
      }
      return b;
   }
   
   protected PublisherBase getTask(String className) throws Exception {
      PublisherBase t = (PublisherBase) Class.forName(className).newInstance();
      return t;      
   }
   
   protected void findPublisherGroup(PublisherSettings publisherSettings) {
      List<PublisherGroup> groups = publisherSettings.getPublisherGroups();
      for (PublisherGroup publisherGroup : groups) {
         if (publisherGroup.getName().equals(testgroup)) {
            pg = publisherGroup;
            break;
         }
      }
   }
   
   protected static int getRepeatCounter(String[] args)
   {
      int repeatCounter = 1;
      if (args.length > 3 && args[3] != null) {
         try {
            repeatCounter = Integer.parseInt(args[3]);
         } catch (Exception e) {
            logger.warn("Invalid value specified for repeatcounter. Using default as 1.");
         }
         logger.info("The test will repeat for [" + repeatCounter + "] times.");
      } else {
         logger.info("No repeatcounter specified. Process will run only once.");         
      }
      return repeatCounter;
   }
   
}
