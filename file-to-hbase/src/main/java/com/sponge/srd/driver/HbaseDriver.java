package com.sponge.srd.driver;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HbaseDriver {
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
        opts.addOption("p", true, "web服务运行端口");

        CommandLineParser parser = new DefaultParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("May Options", opts);
                } else {
                    String port= cl.getOptionValue("p");
                    LOG.info("还没有实现...");
                }
            } else {
                System.err.println("ERROR_NOARGS");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
