package com.sponge.srd.driver;

import com.sponge.srd.utils.FileOpHbase;
import com.sponge.srd.utils.HbaseUtils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class HbaseFileUpload {
    private static  final Logger LOG = LoggerFactory.getLogger(HbaseDriver.class);

    public static void main(String[] args) throws IOException {
        try {
            ExecuteCommandLine(args);
        } catch (IOException e) {
            LOG.error("error " + e.getMessage());
        }
    }

    public static void ExecuteCommandLine(String[] args ) throws IOException {

        Options opts = new Options();
        opts.addOption("h", false, "Help description");
        opts.addOption("d", true, "上传的文件夹,监控此文件夹并上传所有文件");
        opts.addOption("t", true, "扫描文件夹间隔时间,默认1000ms");

        CommandLineParser parser = new DefaultParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("May Options", opts);
                } else {
                    String fullFilePath = cl.getOptionValue("d");
                    int delms = Integer.parseInt(cl.getOptionValue("t", "1000"));
                    while (true) {
                        List<String> files = HbaseUtils.getFiles(fullFilePath);
                        FileOpHbase fileOpHbase = new FileOpHbase();
                        fileOpHbase.writeDataFiles(files, fullFilePath);
                        try {
                            Thread.sleep(delms);
                        } catch (InterruptedException e ) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                System.err.println("ERROR_NOARGS");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
