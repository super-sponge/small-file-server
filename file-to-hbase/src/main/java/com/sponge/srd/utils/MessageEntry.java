package com.sponge.srd.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageEntry { private byte[] content;
    private String rowkey;
    private String mtime;

    public MessageEntry(byte[] content, String rowkey, String mtime) {
        this.content = content;
        this.rowkey = rowkey;
        this.mtime = mtime;
    }
    public MessageEntry(byte[] content, String rowkey) {
        this.content = content;
        this.rowkey = rowkey;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.mtime = df.format(new Date());
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }
}
