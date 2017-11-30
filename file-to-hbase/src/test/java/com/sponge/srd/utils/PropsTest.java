package com.sponge.srd.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class PropsTest {

    @Test
    public void getConfigDir() {
        String confDir = Props.getConfigDir();
        System.out.println("Configuration dir is " + confDir);
    }

    @Test
    public void getConfigProperty() {
        PropertiesConfiguration props = Props.getProperties();
        for (Iterator<String> it = props.getKeys(); it.hasNext(); ) {
            String key = it.next();
            System.out.println("Key " + key + "\tvalue " + props.getString(key));
        }
    }
}