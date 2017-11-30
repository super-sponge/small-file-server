package com.sponge.srd.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by sponge on 2017/6/28.
 */
public class Props {
    private static final Logger LOG = LoggerFactory.getLogger(Props.class);

    private static PropertiesConfiguration properties = null;
    private static String configDir = null;

    public static PropertiesConfiguration getProperties() {
        if ( properties == null ) {
            properties = new PropertiesConfiguration();
            String propsPath  =  getConfigDir() + "api-server.properties";
            LOG.info("Properties path is " + propsPath);
            try {
                properties.load(propsPath);
            }catch (ConfigurationException e ) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static String getConfigDir() {
        if (configDir == null) {
            String propsPath = System.getenv().get("CONF_DIR");
            if(propsPath == null) {
                propsPath = "src" + File.separator +"main" + File.separator+ "conf";
            }
            if (! propsPath.endsWith(File.separator)) {
                propsPath += File.separator;
            }

            configDir = propsPath;
        }
        return  configDir;
    }
}
