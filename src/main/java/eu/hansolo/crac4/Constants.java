package eu.hansolo.crac4;

import java.io.File;


public class Constants {
    public static final String HOME_FOLDER          = new StringBuilder(System.getProperty("user.home")).append(File.separator).toString();
    public static final String PROPERTIES_FILE_NAME = "crac4.properties";
    public static final String INITIAL_CLEAN_DELAY  = "initial_clean_delay";
    public static final String CLEAN_INTERVAL       = "clean_interval";
}
