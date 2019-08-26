package com.ssword.imserver.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;

/**
 * ftp工具类
 * <p>
 * 1> 调用uploadDirectory和downLoadDirectory之后，记得手动调用ftpLogOut方法来关闭链接
 * <p>
 * 2> changeWorkingDirectory(directory)中的directory为 要切换到的目录
 * 可为 -> 绝对路径; 可为 -> 相对路径(如果为相对路径,那么相对于当前session所处目录)
 *
 * @author songJian
 * @version 2019-1-9
 */
public class FtpUtil {
    private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    /**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, String ftpUserName,
                                         String ftpPassword, int ftpPort) {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.error("未连接到FTP，用户名或密码错误。" + ftpUserName + "," + ftpPassword);
                ftpClient.disconnect();
            } else {
                logger.info("FTP连接成功。");
                ftpClient.setControlEncoding("UTF-8"); // 中文支持
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();// 被动模式
                ftpClient.enterLocalActiveMode();    //主动模式
            }
        } catch (SocketException e) {
            e.printStackTrace();
            logger.error("FTP的IP地址可能错误，请正确配置。" + ftpHost + "," + ftpPort);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("FTP的端口错误,请正确配置。");
        }
        return ftpClient;
    }

    /**
     * 向FTP服务器上传文件
     *
     * @param ftpPath  FTP服务器文件路径 格式： ftptest/aa
     * @param fileName ftp文件名称
     * @param input    文件流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(FTPClient ftpClient, String ftpPath,
                                     String fileName, InputStream input) {
        boolean success = false;
        try {
            int reply;
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }
//            ftpClient.makeDirectory(ftpPath);// 只能创建一层目录
            if (!existFileInfos(ftpPath, ftpClient)) {
                logger.info("目录不存在");
                createDirecroty(ftpPath, ftpClient);
            }
            ftpClient.changeWorkingDirectory(ftpPath);
            ftpClient.storeFile(fileName, input);
            input.close();
            ftpLogOut(ftpClient);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * 向FTP服务器上传文件
     *
     * @param ftpHost     FTP服务器hostname
     * @param ftpUserName 账号
     * @param ftpPassword 密码
     * @param ftpPort     端口
     * @param ftpPath     FTP服务器文件路径 格式： ftptest/aa
     * @param fileName    ftp文件名称
     * @param input       文件流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String ftpHost, String ftpUserName,
                                     String ftpPassword, int ftpPort, String ftpPath,
                                     String fileName, InputStream input) {
        FTPClient ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
        return uploadFile(ftpClient, ftpPath, fileName, input);
    }

    public static boolean uploadFile(String ftpHost, String ftpUserName,
                                     String ftpPassword, int ftpPort, String ftpPath, File localFile) {
        FTPClient ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
        return uploadFile(ftpClient, ftpPath, localFile);
    }

    /**
     * 向FTP服务器上传文件
     *
     * @param ftpClient
     * @param romotUpLoadePath 上传服务器路径 - 应该以/结束
     * @param localFile        当地文件
     * @return
     */
    public static boolean uploadFile(FTPClient ftpClient, String romotUpLoadePath, File localFile) {
        try {
            if (!existFileInfos(romotUpLoadePath, ftpClient)) {
                logger.info("目录不存在");
                createDirecroty(romotUpLoadePath, ftpClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean b = uploadFile(ftpClient, localFile, romotUpLoadePath);
        ftpLogOut(ftpClient);
        return b;
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param ftpClient
     * @param remoteFileName
     * @param localDires
     * @param remoteDownLoadPath
     * @return
     */
    public static boolean downloadFtpFile(FTPClient ftpClient, String remoteFileName, String localDires,
                                          String remoteDownLoadPath) {
        boolean b = downloadFile(ftpClient, remoteFileName, localDires, remoteDownLoadPath);
        ftpLogOut(ftpClient);
        return b;
    }

    public static boolean downloadFtpFile(String ftpHost, String ftpUserName,
                                          String ftpPassword, int ftpPort, String remoteFileName, String localDires,
                                          String remoteDownLoadPath) {
        FTPClient ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
        boolean b = downloadFile(ftpClient, remoteFileName, localDires, remoteDownLoadPath);
        ftpLogOut(ftpClient);
        return b;
    }

    /**
     * 上传文件夹(记得退出)
     *
     * @param ftpClient
     * @param basePath            用来在createDirecroty后，文件夹就切换到其指定目录，再进行文件夹的创建就需要替换这一级了，值应该第一次传入的remoteDirectoryPath一样
     * @param localDirectory      当地文件夹
     * @param remoteDirectoryPath Ftp 服务器路径 以目录"/"结束
     * @param num                 调用的时候传0就ok
     * @return
     */
    public static boolean uploadDirectory(FTPClient ftpClient, String basePath, String localDirectory,
                                          String remoteDirectoryPath, int num) {
        File src = new File(localDirectory);
        String inPath = "";
        int nm = 0;
        try {
            if (num == 0) {
                if (!existFileInfos(remoteDirectoryPath, ftpClient)) {
                    logger.info("目录不存在");
                    createDirecroty(remoteDirectoryPath, ftpClient);
                }
            } else {
                remoteDirectoryPath = remoteDirectoryPath + src.getName() + "/";
                inPath = remoteDirectoryPath.replace(basePath, "");
//                inPath = src.getName()+"/";
                nm = makeDirectoryDir(inPath, ftpClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info(remoteDirectoryPath + "目录创建失败");
        }
        File[] allFile = src.listFiles();
        for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if (!allFile[currentFile].isDirectory()) {
                String srcName = allFile[currentFile].getPath().toString();
                if (num == 1) {
                    uploadFile(ftpClient, new File(srcName), inPath);
                    try {
                        ftpClient.changeToParentDirectory();
                        for (int i = 0; i < nm; i++) {
                            ftpClient.changeToParentDirectory();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    uploadFile(ftpClient, new File(srcName), remoteDirectoryPath);
                }
            }
        }
        for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if (allFile[currentFile].isDirectory()) {
                uploadDirectory(ftpClient, basePath, allFile[currentFile].getPath().toString(),
                        remoteDirectoryPath, 1);
            }
        }
        return true;
    }

    /**
     * 下载文件夹(记得退出)
     *
     * @param ftpClient
     * @param localDirectoryPath 本地地址
     * @param remoteDirectory    远程文件夹
     * @return
     */
    public static boolean downLoadDirectory(FTPClient ftpClient, String localDirectoryPath, String remoteDirectory) {
        try {
            if (!localDirectoryPath.endsWith("/") && !localDirectoryPath.endsWith("\\")) {
                localDirectoryPath = localDirectoryPath + "/";
            }
            String fileName = new File(remoteDirectory).getName();
            localDirectoryPath = localDirectoryPath + fileName + "/";
            new File(localDirectoryPath).mkdirs();
            FTPFile[] allFile = ftpClient.listFiles(remoteDirectory);
            for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
                if (!allFile[currentFile].isDirectory()) {
                    downloadFile(ftpClient, allFile[currentFile].getName(), localDirectoryPath, remoteDirectory);
                }
            }
            for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
                if (allFile[currentFile].isDirectory()) {
                    String strremoteDirectoryPath = remoteDirectory + "/" + allFile[currentFile].getName();
                    downLoadDirectory(ftpClient, localDirectoryPath, strremoteDirectoryPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("下载文件夹失败");
            return false;
        }
        return true;
    }

    /**
     * 退出，关闭连接
     *
     * @param ftpClient
     */
    public static void ftpLogOut(FTPClient ftpClient) {
        if (null != ftpClient && ftpClient.isConnected()) {
            try {
                boolean reuslt = ftpClient.logout();// 退出FTP服务器
                if (reuslt) {
                    logger.info("成功退出服务器");
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("退出FTP服务器异常！" + e.getMessage());
            } finally {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.disconnect();// 关闭FTP服务器的连接
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.warn("关闭FTP服务器的连接异常！");
                }
            }
        }
    }

    // 判断ftp服务器文件夹是否存在
    private static boolean existFileInfos(String path, FTPClient ftpClient) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);// 使用默认的系统自动检测机制，获取当前工作目录或单个文件的文件信息列表。
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    private static boolean createDirecroty(String remote, FTPClient ftpClient) throws IOException {
        boolean success = true;
        String directory = remote + "/";
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(new String(directory), ftpClient)) {
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            String path = "";
            String paths = "";
            int jishuqi = 0;
            while (true) {
                jishuqi++;
                if (jishuqi > 1000) {// 如果x次循环都没有正常跳出则强制跳出
                    break;
                }
                String subDirectory = remote.substring(start, end);
                path = path + "/" + subDirectory;
                if (!existFileInfos(path, ftpClient)) {
                    if (makeDirectory(subDirectory, ftpClient)) {
                        changeWorkingDirectory(subDirectory, ftpClient);
                    } else {
                        logger.info("创建目录[" + subDirectory + "]失败");
                        changeWorkingDirectory(subDirectory, ftpClient);
                    }
                } else {
                    changeWorkingDirectory(subDirectory, ftpClient);
                }
                paths = paths + "/" + subDirectory;
                start = end + 1;
                end = directory.indexOf("/", start);
                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return success;
    }

    // 改变目录路径
    private static boolean changeWorkingDirectory(String directory, FTPClient ftpClient) {
        boolean flag = true;
        try {
            flag = ftpClient.changeWorkingDirectory(directory);
            if (flag) {
                logger.info("进入文件夹" + directory + " 成功！");
            } else {
                logger.info("进入文件夹" + directory + " 失败！");

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return flag;
    }

    // 创建目录
    private static int makeDirectoryDir(String dir, FTPClient ftpClient) throws IOException {
        String[] cc = dir.split("/");
        int nm = 0;
        for (int i = 0; i < cc.length; i++) {
            if (StringUtils.isNotBlank(cc[i])) {
                if (i > 0) {
                    nm++;
                    boolean b = ftpClient.changeWorkingDirectory(cc[i-1]);
                    logger.info("==>{}",b);
                }
                boolean flag = ftpClient.makeDirectory(cc[i]);
                if (flag) {
                    logger.info("创建文件夹" + cc[i] + " 成功！");
                } else {
                    logger.info("创建文件夹" + cc[i] + " 失败！");// 失败有可能是因为本文件夹已经存在
                }
            }
        }
        if (nm > 0) {// 返回
            for (int i = 0; i < nm; i++) {
                ftpClient.changeToParentDirectory();
            }
        }
        return nm;
    }

    // 创建目录
    private static boolean makeDirectory(String dir, FTPClient ftpClient) {
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            if (flag) {
                logger.info("创建文件夹" + dir + " 成功！");
            } else {
                logger.info("创建文件夹" + dir + " 失败！");// 失败有可能是因为本文件夹已经存在
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    // 当前目录下是否存在某个文件 true-有 false-无
    private static boolean existFile(FTPClient ftp, String filename) {
        // 检验文件是否存在
        InputStream is = null;
        try {
            is = ftp.retrieveFileStream(new String(filename.getBytes("GBK"), FTP.DEFAULT_CONTROL_ENCODING));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is == null || ftp.getReplyCode() == FTPReply.FILE_UNAVAILABLE) {
            return false;
        }

        if (is != null) {
            try {
                is.close();
                ftp.completePendingCommand(); // 必须执行，否则在循环检查多个文件时会出错
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /***
     * 上传Ftp文件
     * @param localFile 当地文件
     * @param romotUpLoadePath 上传服务器路径 - 应该以/结束
     * */
    private static boolean uploadFile(FTPClient ftpClient, File localFile, String romotUpLoadePath) {
        BufferedInputStream inStream = null;
        boolean success = false;
        String name = null;
        try {
            name = new String(localFile.getName().getBytes("UTF-8"), "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            ftpClient.changeWorkingDirectory(romotUpLoadePath);// 改变工作路径
            if (existFile(ftpClient, name)) {
                logger.info(name + "文件已存在于服务器");
                return true;// 服务器已经有这个资源了
            }
            inStream = new BufferedInputStream(new FileInputStream(localFile));
            logger.debug(name + "开始上传.....");
            success = ftpClient.storeFile(name, inStream);
            if (success == true) {
                logger.debug(name + "上传成功");
                return success;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(name + "未找到");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    /***
     * 下载文件
     * @param remoteFileName 待下载文件名称
     * @param localDires 下载到当地那个路径下
     * @param remoteDownLoadPath remoteFileName所在的路径
     * */
    private static boolean downloadFile(FTPClient ftpClient, String remoteFileName, String localDires,
                                        String remoteDownLoadPath) {
        if (!localDires.endsWith("/") && !localDires.endsWith("\\")) {
            localDires = localDires + "/";
        }
        String strFilePath = localDires + remoteFileName;
        BufferedOutputStream outStream = null;
        boolean success = false;
        try {
            ftpClient.changeWorkingDirectory(remoteDownLoadPath);
            outStream = new BufferedOutputStream(new FileOutputStream(
                    strFilePath));
            logger.info(remoteFileName + "开始下载....");
            success = ftpClient.retrieveFile(remoteFileName, outStream);
            if (success == true) {
                logger.info(remoteFileName + "成功下载到" + strFilePath);
                return success;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(remoteFileName + "下载失败");
        } finally {
            if (null != outStream) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (success == false) {
            logger.error(remoteFileName + "下载失败!!!");
        }
        return success;
    }
}
