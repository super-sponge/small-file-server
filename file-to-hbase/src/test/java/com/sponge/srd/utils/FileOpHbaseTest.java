package com.sponge.srd.utils;

import com.sponge.srd.driver.HbaseFileQuery;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FileOpHbaseTest {
    @Test
    public void writeDataFiles() throws Exception {
        List<String> files = HbaseUtils.getFiles("data");
        FileOpHbase fileOpHbase = new FileOpHbase();
        fileOpHbase.writeDataFiles(files, "data");
    }
    @Test
    public void exportFile() throws Exception {
        String file = "lover\\lover2.jpg";
        FileOpHbase.exportFile(file, "target\\lover2.jpg");
    }

}