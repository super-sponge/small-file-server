package com.sponge.srd.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sponge on 2017/6/28.
 */
public class HbaseUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HbaseUtils.class);

    private static Connection conn = null;
    private static PropertiesConfiguration props = Props.getProperties();
    private static int keyVAlueMaxSize = Integer.MAX_VALUE;
    private static String fileProcessType = null;
    private static String fileProcessBackDir = null;

    private static String HEXDIGITS = "0123456789ABCDEF";
    private static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static ArrayList<String> filelist = new ArrayList<String>();


    public static Connection getConn() throws IOException {
        if (conn == null) {
            Configuration conf = HBaseConfiguration.create();
            conf.addResource(new Path(Props.getConfigDir() + "hdfs-site.xml"));
            conf.addResource(new Path(Props.getConfigDir() + "hbase-site.xml"));
            KerberosUtils.Kerberos(conf);

            //cell 大小根据配置信息获取最大存入hbase的文件大小 + 64K （rowkey最大值）+ 32 (cf 最大值）
            int cellSize = getValueMaxSize() + 64 * 1024 + 32;
            conf.set("hbase.client.keyvalue.maxsize", String.valueOf(cellSize));
            LOG.warn("Please check the hbaseserver  hbase-site.xml  " +
                    "hbase.client.keyvalue.maxsize value should bigger than " + String.valueOf(cellSize));

            conn = ConnectionFactory.createConnection(conf);
        }

        return conn;
    }

    public static Table getTable(String tableName) throws IOException {
        return getConn().getTable(TableName.valueOf(tableName));
    }

    /**
     * 获取MD5值
     */
    public static String md5(String old) {
        try {
            byte[] btInput = old.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取split keys
     *
     * @return
     */
    public static byte[][] genSplitKey() {
        int hexLen = HEXDIGITS.length();
        int splitLen = hexLen * hexLen;
        System.out.println("Splits is " + splitLen);
        byte[][] splits = new byte[splitLen][];
        for (int i = 0; i < hexLen; i++)
            for (int j = 0; j < hexLen; j++) {
                byte[] split = new byte[2];
                split[0] = (byte) HEXDIGITS.charAt(i);
                split[1] = (byte) HEXDIGITS.charAt(j);
                splits[(i * hexLen) + j] = split;
            }
        return splits;

    }

    public static byte[][] genSplitKey(int level) {
        byte[][] splits = null;
        int splitLen = 1;
        for (int i = 0; i < level; i++) {
            splitLen *= HEXDIGITS.length();
        }
        System.out.println("Splits is " + splitLen);
        splits = new byte[splitLen][];

        ArrayList<String> list = new ArrayList<>();
        ArrayList<Character> com = new ArrayList<>();
        getCombinations(list, HEXDIGITS.toCharArray(), 0, level, com);
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);
            splits[i] = Bytes.toBytes(key);
            System.out.println(key);
        }
        return splits;
    }

    private static void getCombinations(ArrayList<String> list, char[] cs, int start, int len, ArrayList<Character> com) {//len为组合的长度
        if (len == 0) {
            String s = "";
            for (int i = 0; i != com.size(); i++) {
                s = s.concat(com.get(i).toString());
            }
            list.add(s);
            return;
        }
        if (start == cs.length) {
            return;
        }
        com.add(cs[start]);
        getCombinations(list, cs, start + 1, len - 1, com);
        com.remove(com.size() - 1);
        getCombinations(list, cs, start + 1, len, com);
    }

    private static int getCountOfCombinations(int arrLen, int len) {//获取长度为len的组合数
        int m = 1;
        for (int i = 0; i != len; i++) {
            m *= arrLen - i;
        }
        int n = 1;
        for (int i = len; i != 1; i--) {
            n *= i;
        }
        return m / n;
    }

    /**
     * 写入byte[] 到 文件
     *
     * @param content  内容
     * @param filePath 文件路劲
     */
    public static void writeFile(byte[] content, String filePath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(content);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param filePath
     * @return
     */
    public static byte[] readFile(String filePath) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = null;

        try {
            fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis);
            int len = 0;

            byte[] buf = new byte[4096];

            while ((len = bis.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            buffer = baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            buffer = null;
        } finally {
            try {
                baos.close();
                bis.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return buffer;
    }

    /**
     * 生成rowkey, 对rowkey 用md5 加密后娶前4位与rowkey组成新的rowkey
     *
     * @param rowkey
     * @return
     */
    public static String rowKeyGen(String rowkey, int size) {
        if (size == 0) {
            return rowkey;
        }
        String md5 = md5(rowkey);
        return md5.substring(0, size) + rowkey;
    }


    /**
     * 获取文件夹下面所有文件列表
     *
     * @param filePath
     * @return
     */
    public static List<String> getFiles(String filePath) {
        filelist.clear();
        getFile(filePath);
        return filelist;
    }

    private static void getFile(String path) {
        // 获得指定文件对象
        File file = new File(path);
        // 获得该文件夹内的所有文件
        File[] array = file.listFiles();

        for (int i = 0; i < array.length; i++) {
            if (array[i].isFile())//如果是文件
            {
                // 输出当前文件的完整路径
                // System.out.println("#####" + array[i]);
                // 同样输出当前文件的完整路径   大家可以去掉注释 测试一下
                // System.out.println(array[i].getPath());
                filelist.add(array[i].getPath());
            } else if (array[i].isDirectory())//如果是文件夹
            {
                //System.out.println(array[i].getPath());
                getFile(array[i].getPath());
            }
        }
    }


    /**
     * 获取bhaee kevalue  能存储的最大值
     *
     * @return
     */
    public static int getValueMaxSize() {
        if (keyVAlueMaxSize == Integer.MAX_VALUE) {
            keyVAlueMaxSize = props.getInt(Constant.HBASE_VALUE_MAX_LENGTH, Constant.DEFAULT_HBASE_VALUE_MAX_LENGTH);
        }
        return keyVAlueMaxSize;
    }
    public static String nowTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }


    public static String getFileProcessType() {
        if (fileProcessType == null) {
            fileProcessType = props.getString(Constant.PRG_FILEPROCESS_TYPE, Constant.DEFAULT_PRG_FILEPROCESS_TYPE);
            fileProcessType = fileProcessType.toLowerCase();
        }
            return fileProcessType;
    }

    public static String getFileProcessBackDir() {
        if (fileProcessBackDir == null) {
            fileProcessBackDir = props.getString(Constant.PRG_FILEPROCESS_FILEBACK_DIR,
                    Constant.DEFAULT_PRG_FILEPROCESS_FILEBACK_DIR);
            if (!fileProcessBackDir.endsWith(File.separator)) {
                fileProcessBackDir = fileProcessBackDir + File.separator;
            }

        }
        return fileProcessBackDir;
    }

    public static void moveFile(String srcFile, String distPath) {
        //todo  windows 下面文件路径去要把文件头去掉
        String distFile = null;
        if ( FileUtils.windowsPath(srcFile)) {
           distFile = srcFile.substring(3);
        } else if (srcFile.startsWith(".")){
            distFile = srcFile.substring(srcFile.indexOf(File.separator));
        }
        distFile = distPath + distFile;
        FileUtils.Move(srcFile,distFile);
    }

}
