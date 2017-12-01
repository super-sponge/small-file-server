package com.sponge.srd.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sponge.srd.utils.HbaseUtils.nowTime;

public class FileOpHbase {
    private static final Logger LOG = LoggerFactory.getLogger(FileOpHbase.class);
    private static org.apache.hadoop.conf.Configuration conf = null;
    private static PropertiesConfiguration props = Props.getProperties();
    int pt = props.getInt(Constant.PORT, Constant.DEFAULT_PORT);
    private static Connection conn = null;
    private static long msgNumbers = 0;
    private static int batchsize = props.getInt(Constant.HBASE_COMMIT_BUFFER_SIZE, 4096);
    private static int keyVAlueMaxSize = Integer.MAX_VALUE;
    private static int rowkeyHeaderSize=props.getInt(Constant.HBASE_TABLE_ROWKEY_HEADER_SIZE,
            Constant.DEFAULT_HBASE_TABLE_ROWKEY_HEADER_SIZE);
    private static byte[] CF = Bytes.toBytes(props.getString(Constant.HBASE_COLUMN_FAMILY, "cf"));
    //存放数据的列
    private static byte[] CV = Bytes.toBytes(props.getString(Constant.HBASE_COLUMN_VALUE, "v"));
    //存放标记的列
    private static byte[] CVF = Bytes.toBytes("f");

    //在标记列存放的值表示存放的是数据
    private static byte[] CDATA = Bytes.toBytes("d");
    //在标记列存放的值表示存放的是文件路径
    private static byte[] CFILE = Bytes.toBytes("f");
    private static TableName tableName = TableName.valueOf(props.getString(Constant.TABLE_NAME_KEY, Constant.DEFAULT_TABLE_NAME));


    //缓存已经插入的文件目录结构
    private static Set<String> cacheDir = ConcurrentHashMap.<String> newKeySet();

    //存储输入的目录
    private String inputPath = "";

    /**
     * 获取MessageEnty 信息插入到hbase
     * 插入hbase的最入口，其他函数都是调用此函数
     *
     * @param messages
     * @throws IOException
     */
    public void writeData(List<MessageEntry> messages) throws IOException {
        List<Put> puts = new ArrayList<>(batchsize);
        Table table = getTable();

        for (MessageEntry msg : messages) {
            if (msg.getContent() == null ) {
                LOG.error("Write to hbase " + msg.getRowkey() + " Failed ");
                StatUtils.statDataFailedToHBaseCnt(1);
                continue;
            }
            LOG.info("process file " + msg.getRowkey());
            int length = msg.getContent().length;
            putFile(new NodeInfo(msg.getRowkey(), msg.getMtime(), length));
            String key =  HbaseUtils.rowKeyGen(msg.getRowkey(), rowkeyHeaderSize);
            if(length > HbaseUtils.getValueMaxSize()) {
                LOG.info(msg.getRowkey() + "  length " + length + " > " + HbaseUtils.getValueMaxSize() + " UptoHdfs");
                writeDataStreamTohdfs(msg.getContent(), msg.getRowkey());
                StatUtils.statDataSucceedToHdfs(1);
                continue;
            }
            Put put = new Put(Bytes.toBytes(key));
            put.addColumn(CF, CV, msg.getContent());
            put.addColumn(CF, CVF, CDATA);
            puts.add(put);
            if (puts.size() >= batchsize) {
                batchCommitMsg(table, puts);
            }
        }

        if (puts.size() > 0) {
            batchCommitMsg(table, puts);
        }

        table.close();

    }

    /**
     * 批量提交puts中数据到hbase
     *
     * @param table
     * @param puts
     * @throws IOException
     */
    private void batchCommitMsg(Table table, List<Put> puts) throws IOException {
        try {
            table.put(puts);
        } catch (IOException ex) {
            for (Put put : puts) {
                List<Cell> cells = put.get(CF, CV);
                Cell cell = cells.get(0);
                LOG.error("Failed send message " + this.inputPath +  Bytes.toString(CellUtil.cloneValue(cell)));
            }
            StatUtils.statDataFailedToHBaseCnt(puts.size());
            puts.clear();
            LOG.debug("Clean buffers ");
            return;
        }

        StatUtils.statDataSucceedToHBase(puts.size());

        for (Put put : puts) {
            List<Cell> cells = put.get(CF, CV);
            Cell cell = cells.get(0);
            String srcFile = this.inputPath + Bytes.toString(CellUtil.cloneValue(cell));
            LOG.info("succed send message " + srcFile);
            if (HbaseUtils.getFileProcessType() == "delete") {
                FileUtils.deleteFile(srcFile);
            } else if (HbaseUtils.getFileProcessType() == "move"){
                HbaseUtils.moveFile(srcFile, HbaseUtils.getFileProcessBackDir());
            } else {
                LOG.warn("File " + srcFile + " not move or delete!!!");
            }
        }
        msgNumbers += puts.size();
        LOG.info("Commit message " + puts.size() + " Total message " + msgNumbers);
        LOG.debug("Clean buffers ");
        puts.clear();
    }

    /**
     * 返回对应rowke的value 并生成文件
     *
     * @param rowkey 如果存入的值文件名，直接输入文件名
     * @param localFile
     * @throws IOException
     */
    public static void exportFile(String rowkey, String localFile) throws IOException {
        byte[] content = exportFile(rowkey);
        if (content != null) {
            HbaseUtils.writeFile(content, localFile);
        } else {
            LOG.info("not find rowkey " + rowkey);
        }
    }

    /**
     * 获取rowkey 对应的值并
     * 根据rowk 返回CF:CV的byte[] 值, 判断cf:f 为 f , 提取cf:v的值作为hdfs文件路径，读取文件，如果cf:f为d，直接读取cf:v
     *
     * @param rowkey
     * @return 次rowk对应的 value
     * @throws IOException
     */
    public static byte[] exportFile(String rowkey) throws IOException {
        Table table = getConn().getTable(tableName);
        Get get = new Get(Bytes.toBytes(HbaseUtils.rowKeyGen(rowkey,rowkeyHeaderSize)));
        get.addFamily(CF);
        byte[] content = null;
        boolean hdfsFlag = false;
        Result result = table.get(get);
        for (Cell cell : result.rawCells()) {
            byte[] col = CellUtil.cloneQualifier(cell);
            if (Arrays.equals(col, CV)) {
                content = CellUtil.cloneValue(cell);
            }
            if(Arrays.equals(col, CVF)) {
                hdfsFlag = Arrays.equals(CFILE , CellUtil.cloneValue(cell));
            }
        }

        if (hdfsFlag) {
            String fileName = new String(content);
            LOG.info("获取hdfs相应文件: " + fileName);
            content = HdfsUtils.getFile(fileName);
        }
        table.close();
        return content;
    }

    /**
     * 获取hbase 连接，判断表是否存在，如果不存在创建表
     *
     * @return
     * @throws IOException
     */
    private static Connection getConn() throws IOException {
        if (conn == null) {
            conn = HbaseUtils.getConn();
        }

        Admin admin = null;
        try {
            admin = conn.getAdmin();
            if (!admin.tableExists(tableName)) {
                HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
                tableDescriptor.addFamily(new HColumnDescriptor(CF));
                admin.createTable(tableDescriptor, HbaseUtils.genSplitKey());
            }
            admin.close();
        }catch (TableExistsException e ) {
            if (admin != null ) {
                admin.close();
            }
        }
        return conn;
    }

    /**
     * 根据文件列表，读取每个文件内容，插入到hbase
     *
     * @param lstFilesPath
     * @throws IOException
     */
    public void writeDataFiles(List<String> lstFilesPath, String parentDir) throws IOException {
        this.inputPath = parentDir.endsWith(File.separator) ? parentDir : parentDir + File.separator;
        List<MessageEntry> lstMessage = new ArrayList<MessageEntry>(lstFilesPath.size());
        for (String file : lstFilesPath) {
            byte[] value = HbaseUtils.readFile(file);
            lstMessage.add(new MessageEntry(value, file.substring(this.inputPath.length()), nowTime()));
        }
        writeData(lstMessage);
    }

    /**
     * 读取文件流数据，上传到hdfs
     * @param in
     * @param fileName
     * @throws IOException
     */
    private void writeDataStreamTohdfs(InputStream in, String fileName) throws IOException {
        String hdfsFile = HdfsUtils.getKeepDir() +  HbaseUtils.md5(fileName);
        HdfsUtils.upLoadFile(in, hdfsFile);
        Put put = new Put(Bytes.toBytes(HbaseUtils.rowKeyGen(fileName, rowkeyHeaderSize)));
        put.addColumn(CF, CVF, CFILE);
        put.addColumn(CF, CV, Bytes.toBytes(hdfsFile));
        Table table = getTable();
        table.put(put);

    }

    /**
     * 读取字节数组，上传到hdfs
     * @param data
     * @param fileName
     * @throws IOException
     */
    private void writeDataStreamTohdfs(byte[] data, String fileName) throws IOException {
        String hdfsFile = HdfsUtils.getKeepDir() +   HbaseUtils.md5(fileName);
        LOG.info("begin write " + fileName + " to hdfs " + hdfsFile);
        HdfsUtils.uploadFileByte(data, hdfsFile);
        Put put = new Put(Bytes.toBytes(HbaseUtils.rowKeyGen(fileName,rowkeyHeaderSize)));
        put.addColumn(CF, CVF, CFILE);
        put.addColumn(CF, CV, Bytes.toBytes(hdfsFile));
        Table table = getTable();
        table.put(put);
        table.close();
    }

    /**
     * 查询hbase检查key是否存在
     * @param rowKey
     * @return
     * @throws IOException
     */
    public boolean rowKeyExists(String rowKey, String subdata) throws IOException {
        Get get = new Get(Bytes.toBytes(rowKey));
        get.addColumn(CF, Bytes.toBytes(subdata));
        Result result = getTable().get(get);
        return !result.isEmpty();
    }

    /**
     * 插入hbase rowkey
     * @param rowkey
     * @param subdata
     * @param size
     * @throws IOException
     */
    public void putData(String rowkey, String subdata, long size, String mtime ) throws IOException {
        Put  put = new Put(Bytes.toBytes(rowkey));
        String data = mtime + "|" + size;
        put.addColumn(CF, Bytes.toBytes(subdata), Bytes.toBytes(data));
        getTable().put(put);
    }

    /**
     * 获取文件路径解析后插入hbase
     * @param nodeInfo
     * @throws IOException
     */

    public void putFile(NodeInfo nodeInfo) throws IOException {
        String fileName = nodeInfo.getName();
        long size = nodeInfo.getSize();
        int lastSepPos = fileName.lastIndexOf(File.separator);
        String  parentDir = ".";
        String subData = fileName;
        if (lastSepPos != -1 ) {
            parentDir = fileName.substring(0, lastSepPos);
            subData = fileName.substring(lastSepPos + 1);

        }
        putData(parentDir, subData, size, nodeInfo.getMtime());
        lastSepPos = parentDir.lastIndexOf(File.separator);
        while(lastSepPos != -1 && lastSepPos != 0 ) {
            subData = parentDir.substring(lastSepPos + 1);
            parentDir = parentDir.substring(0, lastSepPos);

            lastSepPos = parentDir.lastIndexOf(File.separator);
            if(! cacheDir.contains(parentDir + File.separator + subData)) {
                if (rowKeyExists(parentDir, subData)) {
                    cacheDir.add(parentDir + File.separator + subData);
                    break;
                } else {
                    putData(parentDir, subData, 0, nodeInfo.getMtime());
                    cacheDir.add(parentDir + File.separator + subData);
                }
            } else {
                break;
            }
        }
    }

    /**
     * 获取rowkey指定的文件目录和文件
     * @param rowKey
     * @return
     * @throws IOException
     */
    public List<NodeInfo> getRowKeys(String rowKey ) throws IOException {
        List<NodeInfo> files = new ArrayList<NodeInfo>();
        Get get = new Get(Bytes.toBytes(rowKey));
        get.addFamily(CF);
        Result result = getTable().get(get);
        for(Cell cell: result.rawCells()) {
            String data = Bytes.toString(CellUtil.cloneValue(cell));
            int index = data.lastIndexOf('|');
            long size = Long.parseLong(data.substring(index + 1));
            String mtime = data.substring(0, index);
            String fileName = Bytes.toString(CellUtil.cloneQualifier(cell));
            files.add(new NodeInfo(fileName, mtime, size));
        }
        return files;
    }

    private Table getTable() throws IOException {
        return getConn().getTable(tableName);
    }
}
