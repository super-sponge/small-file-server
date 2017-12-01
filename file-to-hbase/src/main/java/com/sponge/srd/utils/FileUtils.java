package com.sponge.srd.utils;

import java.io.*;

public class FileUtils {
    private static String matches = "[A-Za-z]:\\\\[^:?\"><*]*";
    private static boolean flag = false;
    private static File file;

    /**
     * 删除文件或目录
     * @param deletePath 输入可以是文件，也可以是目录
     * @return
     */

    public static boolean DeleteFolder(String deletePath) {
        flag = false;
        file = new File(deletePath);
        if (!file.exists()) {// 判断目录或文件是否存在
            return flag; // 不存在返回 false
        } else {
            if (file.isFile()) {// 判断是否为文件
                return deleteFile(deletePath);// 为文件时调用删除文件方法
            } else {
                return deleteDirectory(deletePath);// 为目录时调用删除目录方法
            }
        }
    }

    /**
     * 删除单个文件
     * @param filePath 文件路径
     * @return
     */
    public static boolean deleteFile(String filePath) {
        flag = false;
        file = new File(filePath);
        if (file.isFile() && file.exists()) {// 路径为文件且不为空则进行删除
            file.delete();// 文件删除
            flag = true;
        }
        return flag;
    }

    /***
     * 删除单个文件夹，包括文件夹下面的子文件夹
     * @param dirPath 文件夹路径
     * @return
     */
    public static boolean deleteDirectory(String dirPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        File dirFile = new File(dirPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();// 获得传入路径下的所有文件
        for (int i = 0; i < files.length; i++) {// 循环遍历删除文件夹下的所有文件(包括子目录)
            if (files[i].isFile()) {// 删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                System.out.println(files[i].getAbsolutePath() + " 删除成功");
                if (!flag)
                    break;// 如果删除失败，则跳出
            } else {// 运用递归，删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;// 如果删除失败，则跳出
            }
        }
        if (!flag)
            return false;
        if (dirFile.delete()) {// 删除当前目录
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建单个文件
     * @param filePath
     * @return
     * @throws IOException
     */
    public static boolean createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {// 判断文件是否存在
            System.out.println("目标文件已存在" + filePath);
            return false;
        }
        if (filePath.endsWith(File.separator)) {// 判断文件是否为目录
            System.out.println("目标文件不能为目录！");
            return false;
        }
        if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
            // 如果目标文件所在的文件夹不存在，则创建父文件夹
            System.out.println("目标文件所在目录不存在，准备创建它！");
            if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                System.out.println("创建目标文件所在的目录失败！");
                return false;
            }
        }
        try {
            if (file.createNewFile()) {// 创建目标文件
                System.out.println("创建文件成功:" + filePath);
                return true;
            } else {
                System.out.println("创建文件失败！");
                return false;
            }
        } catch (IOException e) {// 捕获异常
            e.printStackTrace();
            System.out.println("创建文件失败！" + e.getMessage());
            return false;
        }
    }

    /**
     * 创建目录
     * @param destDirName
     * @return
     */
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {// 判断目录是否存在
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
            destDirName = destDirName + File.separator;
        }
        if (dir.mkdirs()) {// 创建目标目录
            System.out.println("创建目录成功！" + destDirName);
            return true;
        } else {
            System.out.println("创建目录失败！");
            return false;
        }
    }

    /**
     * 移动文件
     * @param srcFile
     * @param destPath
     * @return
     */
    public static boolean Move(String srcFile, String destPath)
    {
        // File (or directory) to be moved
        File file = new File(srcFile);
        if (!file.exists()) {
            System.out.println("file " + srcFile + " not exists");
            return false;
        }
        createDir(getPath(destPath));

        // Move file to new directory
        boolean success = file.renameTo(new File(destPath));
        if (!success) {
            System.out.println("copy " + srcFile + " to " + destPath);
           copyFile(srcFile, destPath);
           System.out.println("delete " + srcFile);
           deleteFile(srcFile);
        }

        return success;
    }

    public static void copyFile(String srcPath, String distPath) {
        try {
            int byteread = 0;
            File oldfile = new File(srcPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(srcPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(distPath);
                byte[] buffer = new byte[4096];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

        /**
         * 判断是否位windows目录
         * @param filePath
         * @return
         */

    public static boolean windowsPath(String filePath) {
        if (filePath.matches(matches)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getPath(String srcFile) {
        int start = srcFile.lastIndexOf(File.separator);
        int end = srcFile.lastIndexOf(".");
        if (end == -1 || start > end) {
            return srcFile;
        } else {
            return srcFile.substring(0, start);
        }
    }

}
