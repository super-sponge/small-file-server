package com.sponge.srd.driver;

import com.sponge.srd.utils.FileOpHbase;
import com.sponge.srd.utils.NodeInfo;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class HbaseFileQuery {
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
        opts.addOption("f", true, "hbase中文件全路径");
        opts.addOption("d", true, "本地文件路径,如果没有指定，查询[f] 指定的文件夹的所有子文件");

        CommandLineParser parser = new DefaultParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("May Options", opts);
                } else {
                    String rowKey = cl.getOptionValue("f");
                    String fullFilePath = cl.getOptionValue("d");
                    if (rowKey!=null && fullFilePath != null ) {
                        LOG.info("Export file " + rowKey + " to " + fullFilePath);
                        FileOpHbase.exportFile(rowKey, fullFilePath);
                    } else if (rowKey != null && fullFilePath == null) {
                        LOG.info("Query dirs " + rowKey);
                        FileOpHbase fileOpHbase = new FileOpHbase();
                        List<NodeInfo> nodes = fileOpHbase.getRowKeys(rowKey);
                        for(NodeInfo nodeInfo: nodes) {
                            System.out.println(nodeInfo);
                        }

                    } else {
                        LOG.error("f 选项必须设定");
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
