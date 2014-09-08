package org.selliott.atm.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Configuration class that wraps atm.properties. TODO: It might be better if
 * this was more dynamic, that it had a "get" method to read configuration
 * parameters, but hard coding the configuration parameters makes the code a bit
 * less verbose.
 */
public class Config {
    public static Config cfg;
    private static final String DEFAULT_FNAME = "atm.properties";

    /**
     * Get an instance with the default INI name.
     * 
     * @return
     * @throws ATMException
     */
    public static synchronized Config getInstance() throws ATMException {
        return getInstance(DEFAULT_FNAME);
    }

    /**
     * Get an instance with a specific INI name.
     * 
     * @param fName
     *            Name of the INI file to load.
     * @return
     * @throws ATMException
     */
    public static synchronized Config getInstance(String fName)
            throws ATMException {
        if (cfg == null) {
            cfg = new Config(fName);
        }
        return cfg;
    }

    /**
     * Get an instance with a list of possible names. This is a convienence
     * function so that "args" may be passed in from "main".
     * 
     * @param args
     *            Possible INI file names.
     * @return
     * @throws ATMException
     */
    public static synchronized Config getInstance(String[] args)
            throws ATMException {
        if (args.length == 0) {
            return getInstance();
        } else {
            for (String arg : args) {
                if (new File(arg).exists()) {
                    return getInstance(arg);
                }
            }
            throw new ATMException(
                    "None of the configuration files specified exist.");
        }
    }

    public String dbURL;
    public String serverURL;

    /**
     * A simple class to hold configuration parameters.
     * 
     * @param fName
     *            Name of the INI file to load.
     * @throws ATMException
     *             if the INI file could not be loaded.
     */
    private Config(String fName) throws ATMException {
        try {
            Properties cfg = new Properties();
            cfg.load(new FileInputStream(fName));
            dbURL = (String) cfg.get("dbURL");
            serverURL = (String) cfg.get("serverURL");
        } catch (Exception e) {
            throw new ATMException("Unable to read configuration file \""
                    + "\".", e);
        }
    }
}
