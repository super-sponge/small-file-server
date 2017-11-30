package com.sponge.srd.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class HdfsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsUtils.class);

    private static FileSystem fs;
    private static Configuration configuration;
    private static PropertiesConfiguration props = Props.getProperties();

    static {
        configuration = new Configuration();
        configuration.addResource(new Path(Props.getConfigDir() + "hdfs-site.xml"));
        configuration.addResource(new Path(Props.getConfigDir() + "core-site.xml"));
        KerberosUtils.Kerberos(configuration);

        try {
            fs = FileSystem.get(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileSystem getFS() {
        return fs;
    }


    public static String getKeepDir() throws IOException {

        String keepDir = props.getString(Constant.KEEP_HDFS_FILE_PATH, "/tmp");
        if( ! keepDir.endsWith("/")) {
            keepDir = keepDir + "/";
        }

        if (!getFS().isDirectory(new Path(keepDir))) {
            mkdir(keepDir);
        }
        return keepDir;
    }

    public static Configuration getConf() {
        return configuration;
    }

    public static FSDataOutputStream getFSDataOutputStream(String fileName) throws IllegalArgumentException, IOException {
        return getFS().create(new Path(fileName));
    }

    public static void upLoadFile(InputStream in, String fileName) throws IOException {
        FSDataOutputStream fsout = getFSDataOutputStream(fileName);
        byte[] buffer = new byte[1024];
        int readbytes = 0;
        while ((readbytes = in.read(buffer)) > 0) {
            fsout.write(buffer, 0, readbytes);
        }
        fsout.close();
    }

    public static void uploadFileByte(byte[] data, String fileName) throws IOException {
        FSDataOutputStream fsout = getFSDataOutputStream(fileName);
        fsout.write(data, 0, data.length);
        fsout.close();
    }

    public static byte[] getFile(String fileName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = getFS().open(new Path(fileName));
        IOUtils.copyBytes(in, baos,4096,true);
        return  baos.toByteArray();
    }

    public static boolean mkdir(String path) throws IllegalArgumentException, IOException {
        return getFS().mkdirs(new Path(path));
    }

}
