package com.persistent.bcsuite.support;

import org.apache.log4j.Logger;

import com.persistent.bcsuite.beans.PublisherGroup;

/**
 * This is a utility class providing some basic validation methods for the suite.
 * 
 * 
 */
public class ValidatorUtil {
   private static final Logger logger = Logger.getLogger(ValidatorUtil.class);

   public static boolean isGroupValid(PublisherGroup pg) {
      if (pg == null) {
         logger.error("No publishers found for group");
         return false;
      }

      if (pg.getTotalPublishers() == 0) {
         logger.error("Publishers count is zero !!");
         return false;
      }

      return true;
   }

   public static boolean areArgumentsValid(String[] args) {
      if (args.length < 3) {
         System.out.println("You need to specify 2 mandatory parameters to the program");
         System.out.println("Example : <programname> <taskkey> <testgroupname> <runtoken> <repeatcounter>");
         System.out
                  .println("<taskkey> MANDATORY This is used to pick up the Java class to use for sending/recv message");
         System.out
                  .println("<testgroupname> MANDATORY This specifies which group configuration should be used from the config file");
         System.out
                  .println("<runtoken> MANDATORY This is used to identify the run output in the DB. You can tie multiple runs using a same token if you wish");
         System.out
                  .println("<repeatcounter> OPTIONAL This specifies how many times should the test be run with the same params. Default is 1");
         System.out.println("Example : run.sh kafka vanillatest 60sizetest");
         System.out.println("Example(to repeat twice) : run.sh kafka vanillatest 60sizetest 2");
         return false;
      }
      return true;
   }
}
