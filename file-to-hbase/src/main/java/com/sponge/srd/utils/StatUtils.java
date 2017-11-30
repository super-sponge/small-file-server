package com.sponge.srd.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatUtils {

    private static final Logger log = LoggerFactory.getLogger(StatUtils.class);
    static long dataFailedToHBaseCnt = 0;
    static long dataSucceedToHBaseCnt = 0;
    static long dataSucceedToHdfsCnt = 0;

    public static void init(final long interval /*秒*/){
        Thread statThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                log.info("启动统计线程");
                while(true){
                    try {
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        log.info("退出统计线程");
                        break;
                    }

                    String result = "数据保存到HBase成功个数:" + dataSucceedToHBaseCnt + "," +
                            "数据提交到HBase失败个数:" + dataFailedToHBaseCnt+ "," +
                            "数据保存到HDSF个数:" + dataSucceedToHdfsCnt + ",";
                    log.info(result);
                }
            }
        });

        statThread.start();
    }

    public static void statDataFailedToHBaseCnt(long size) {
        StatUtils.dataFailedToHBaseCnt += size;
    }

    public static void statDataSucceedToHBase(long size) {
        StatUtils.dataSucceedToHBaseCnt += size;
    }

    public static void statDataSucceedToHdfs(long size) {
        StatUtils.dataSucceedToHdfsCnt += size;
    }

}
