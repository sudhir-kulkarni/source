package com.persistent.bcsuite.support;

import org.apache.log4j.Logger;

public class Validator {
   private static final Logger logger = Logger.getLogger(Validator.class);

   public static boolean areArgumentsValid(String[] args) {
      if (args.length < 3) {
         System.out.println("You need to specify 3 mandatory parameters to the program");
         System.out.println("Example : <programname> <taskkey> <testgroupname> <runtoken> <repeatcounter>");
         System.out.println("<taskkey> MANDATORY This is used to pick up the Java class to use for recieving message");
         System.out
                  .println("<testgroupname> MANDATORY This specifies which group configuration should be used from the config file");
         System.out
                  .println("<runtoken> MANDATORY This is used to identify the run output in the DB. You can tie multiple runs using a same token if you wish");
         System.out
                  .println("<repeatcounter> OPTIONAL This specifies how many times should the test be run with the same params. Default is 1");
         System.out.println("Example : run.sh kafka vanillatest varysize");
         System.out.println("Example(to repeat twice) : run.sh kafka vanillatest varysize 2");
         return false;
      }
      logger.info("################################ Subscriber Test Process started ################################ ");
      logger.info("Using command line parameters:[taskkey=" + args[0] + "],[testgroupname=" + args[1] + "],[token="
               + args[2] + "].");
      return true;
   }
   
   public static int getRepeatCounter(String[] args)
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
   
   

