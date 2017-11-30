package com.sponge.srd.utils;

public class NodeInfo {enum FileType {
    DIRECTORY, FILE
}

    private String name;
    private FileType fileType;
    private long size;
    private String mtime;

    public NodeInfo(String name, String mtime, long size) {
        this.name = name;
        this.size = size;
        this.mtime = mtime;
        if (size <= 0 ) {
            fileType = FileType.DIRECTORY;
        } else  {
            fileType = FileType.FILE;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return fileType == FileType.DIRECTORY;
    }

    public boolean isFile() {
        return fileType == FileType.FILE;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "name='" + name + '\'' +
                ", fileType=" + (fileType == FileType.FILE ? "file" : "dir") +
                ", size=" + size +
                ", mtime='" + mtime + '\'' +
                '}';
    }
}