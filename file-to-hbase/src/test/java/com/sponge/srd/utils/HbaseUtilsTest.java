package com.sponge.srd.utils;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class HbaseUtilsTest {
    @Test
    public void getFiles() throws Exception {
        String path = "src\\main";
        path = path.endsWith(File.separator) ? path : path + File.separator;
        List<String> files = HbaseUtils.getFiles(path);
        for(String file : files) {
            System.out.println(file + ": " + file.substring(path.length()));
        }

    }


}