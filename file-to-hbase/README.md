
## hbase 存储小文件
    本项目演示在hbase中存储小文件。对于大于参数指定的文件，在hbase中存储路径，文件内容直接存储在hdfs上面。
    
## 程序特点
    1. 在存储到hbase时，通过计算key的hash指，取前面N位加在rowkey前面从而避免存入数据热点问题
    2. 对文件路径的结构存储到hbase中。可以通过接口查看FileOpHbase.getRowKeys 函数查看指定文件夹下面的文件和文件夹。
    3. 本程序可以用于把存放在服务器上面的文件存储到hbase，并保留文件在服务器上面的目录结构
    4. hbase 中得表不用手工创建，程序会自动创建预分区表
    
## 程序部署与验证
### 程序部署
    解压 file-to-hbase-1.0-package.tar.gz 文件
    编辑conf下面的配置文件（具体编辑项目请看配置文件上面的介绍)
### 程序运行
####上传文件到hbase
    上传是rowkey 为../data此目录下的路径 + 此路径的hash值得前table.rowkey.header.size 位
    ./upload.sh  -d ../data
####查询hbase中 ../data 有多少文件
    ./query.sh -f  lover
####导出hbase中的文件到本地
    ./query.sh -f   girl1.jpg -d girl1.jpg
     
## 遗留问题
    1. 需要开发restfull 风格的服务HbaseDriver.java 还未实现

## windows 下运行flume-ng
### 在windows机器上面配置jdk,
    JAVA_HOM=C:\Program Files\Java\jdk1.7.0_67
    CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;
    Path是追加变量
    Path=%JAVA_HOME%\bin;%JAVA_HOME%\jre\bin;
### 将hadoop的路径配置到path变量 
    a,解压hadoop-2.6.3.tar.gz
    b,追加环境变量
    Path=D:\hadoop-2.6.3\hadoop-2.6.3\bin;
### 在windows机器上面安装flume
#### 解压apache-flume-1.7.0-bin.rar
#### 在解压过后的目录下有一个子目录conf，在该目录下新建一个flume.conf文件。文件内容为
    a1.sources = r1
    a1.sinks = k1
    a1.channels = c1
    
    # Describe/configure the source
    a1.sources.r1.type = spooldir
    a1.sources.r1.channels = c1
    #需要扫描的目录
    a1.sources.r1.spoolDir = E:\\tmp\\data
    a1.sources.r1.deserializer = org.apache.flume.sink.solr.morphline.BlobDeserializer$Builder
    a1.sources.r1.fileHeader = true
    a1.sources.r1.fileHeaderKey = key
    a1.sources.r1.recursiveDirectorySearch = true
    a1.sources.r1.pollDelay = 1
    a1.sources.r1.batchSize = 100
    #a1.sources.r1.deletePolicy = immediate
    a1.sources.r1.consumeOrder = random
    a1.sources.r1.trackerDir = .flumespool2
    
    # Describe the sink
    #a1.sinks.k1.type = logger
    a1.sinks.k1.type = thrift
    a1.sinks.k1.channel = c1
    # 配置远程调用的IP和端口
    a1.sinks.k1.hostname = 10.0.8.152
    a1.sinks.k1.port = 4545
    
    # Use a channel which buffers events in memory
    a1.channels.c1.type = memory
    a1.channels.c1.capacity = 1000
    a1.channels.c1.transactionCapacity = 100
    
    # Bind the source and sink to the channel
    a1.sources.r1.channels = c1
    a1.sinks.k1.channel = c1
#### 修改启动参数
    打开将bin目录下flume-ng文件，将-Xmx20m选项修改为-Xmx4000m
## linux 下配置接收端

### 上传file-to-hbase-1.0.jar 到flume的lib下面，如果是hdp平台，请放置在/usr/hdp/current/flume-server/lib/ 路径
###修改配置flume.conf
    a1.sources = r1
    a1.sinks = k1 k2
    a1.channels = c1 c2
    
    # Describe/configure the source
    a1.sources.r1.type = thrift
    a1.sources.r1.channels = c1
    a1.sources.r1.bind = 0.0.0.0
    a1.sources.r1.port = 4545
    
    # Describe the sink
    a1.sinks.k1.type = logger
    
    a1.sinks.k2.type = asynchbase
    #需要与hbase中的表和列族对应
    a1.sinks.k2.table = files
    a1.sinks.k2.columnFamily = cf
    a1.sinks.k2.serializer = com.sponge.srd.flume.SPAsyncHbaseEventSerialize
    #rowKey hash 值的前缀
    a1.sinks.k2.serializer.rowkeyheadersize = 2 
    a1.sinks.k2.zookeeperQuorum = sdc2.sefon.com
    a1.sinks.k2.znodeParent=/hbase-secure
    a1.sinks.k2.kerberosPrincipal = hbase/sefon@SEFON.COM
    a1.sinks.k2.kerberosKeytab = /tmp/client.keytab
    
    # Use a channel which buffers events in memory
    a1.channels.c1.type = file
    #a1.channels.c1.capacity = 10000
    #a1.channels.c1.transactionCapacity = 1000
    a1.channels.c1.checkpointDir = ~/.flume1/file-channel/checkpoint1
    a1.channels.c1.dataDirs = ~/.flume1/file-channel/mnt/flume/data
    
    
    a1.channels.c2.type = file
    #a1.channels.c2.capacity = 10000
    #a1.channels.c2.transactionCapacity = 1000
    a1.channels.c2.checkpointDir = ~/.flume2/file-channel/checkpoint2
    a1.channels.c2.dataDirs = ~/.flume2/file-channel/mnt/flume/data
    
    # Bind the source and sink to the channel
    a1.sources.r1.channels = c1 c2
    a1.sinks.k1.channel = c1
    a1.sinks.k2.channel = c2
### 在“高级配置 flume-evn”下面的“flume-env模板”中添加一行“export JAVA_OPTS="-Xms1000m -Xmx4000m -Dcom.sun.management.jmxremote"”
## 在hbase中创建表
###下面的shell 取MD5值前两位构建分区文件，shell如下
    d='0 1 2 3 4 5 6 7 8 9 A B C D E F'
    for i in $d
    do
    for j in $d
    do
    echo $i$j
    done
    done
    将上面shell内容保存为 sp.sh
    执行bash sp.sh > /tmp/split.txt
### 在hbase 里面创建预分区后的hbase表，使用上面创建好的分区文件/tmp/split.txt
    create ' foo_table1:vfiles_splits', ' bar_cf ', SPLITS_FILE => '/tmp/split.txt'
##启动程序
    在windows系统中点击“开始”，在“运行”中输入“cmd”。通过cd命令进入第3步flume的解压目录，执行命令：
    bin\flume-ng agent -n a1 -c conf -f conf/flume.conf
