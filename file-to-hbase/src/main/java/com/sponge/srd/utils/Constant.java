package com.sponge.srd.utils;

/**
 * Created by sponge on 2017/6/28.
 */
public class Constant {
    public static final String PORT = "port";
    public static final int DEFAULT_PORT = 4567;

    public static final String MAX_THREADS = "maxThreads";
    public static final int DEFAULT_MAX_THREADS = 8;


    public static final String MIN_THREADS = "minThreads";
    public static final int DEFAULT_MIN_THREADS = 2;

    public static final String TIMEOUTMILLIS = "timeOutMillis";
    public static final int DEFAULT_TIMEOUTMILLIS = 30000;

    //Kerberosx相关设置
    public static final String KERBEROS_ENABLE ="kerberos.enable";
    public static final boolean DEFAULT_KERBEROS_ENABLE = false;


    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";
    public static final String JAVA_SECURITY_KRB5_KDC="java.security.krb5.kdc";
    public static final String JAVA_SECURITY_KRB5_REALM="java.security.krb5.realm";
    public static final String KEYTAB_FILE="keytab.file";
    public static final String KERBEROS_PRINCIPAL="kerberos.principal";

    //Hbase 设置
    public static final String TABLE_NAME_KEY = "table.name";
    public static final String DEFAULT_TABLE_NAME = "sefon:cdr";
    public static final String HBASE_COMMIT_BUFFER_SIZE = "batchsize";
    public static final String HBASE_VALUE_MAX_LENGTH = "hbase.client.keyvalue.maxsize";
    public static final int DEFAULT_HBASE_VALUE_MAX_LENGTH = 1024*1024;


    public static final String HBASE_COLUMN_FAMILY = "table.cf";
    public static final String HBASE_COLUMN_VALUE = "table.value";
    public static final String HBASE_TABLE_ROWKEY_HEADER_SIZE = "table.rowkey.header.size";
    public static final int DEFAULT_HBASE_TABLE_ROWKEY_HEADER_SIZE = 0;

    //hdfs 配置
    public static final String KEEP_HDFS_FILE_PATH = "hdfs.path";



}
