package com.ssword.imserver.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ssword.imserver.config.SystemConfig;
import com.ssword.imserver.model.FConfig;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 上传工具类
 *
 * @author songJian
 * @version 2018-3-29
 */
public class UpLoadUtils {

    private static final Logger logger = LoggerFactory.getLogger(UpLoadUtils.class);

    public static List<Map> upload(MultipartFile[] files, String path) {
        List<Map> list = Lists.newArrayList();
        for (MultipartFile item : files) {
            Map m = new HashMap();
            m.put("path", upload(item, path));
            m.put("name", item.getOriginalFilename());
            if (m.get("path") == null) {
                continue;
            }
            list.add(m);
        }
        return list;
    }

    // 本方法主要用在导入excel的时候，pantheon直接放在本地进行处理，处理完之后再根据配置看是否需要上传到ftp服务器进行共享。
    public static String uploadLocal(MultipartFile file, String path, String md5) {
        path = path + Utils.getPathByMD5(md5) + "/";
        return uploadLocal(file, path);
    }

    public static String upload(MultipartFile file, String path, String md5) {
        path = path + Utils.getPathByMD5(md5) + "/";
        if ("1".equals(SystemConfig.getProperty("upload.mode"))) {
            return uploadLocal(file, path);
        } else {
            return uploadFtp(file, path);
        }
    }

    public static String upload(MultipartFile file, String path) {
        String md5 = Utils.getMD5ByFile(file);
        path = path + Utils.getPathByMD5(md5) + "/";
        if ("1".equals(SystemConfig.getProperty("upload.mode"))) {
            return uploadLocal(file, path);
        } else {
            return uploadFtp(file, path);
        }
    }

    // 同步(做这个是因为ftp时富文本需要同步返回，不然图片不显示)
    public static String ueUpload(MultipartFile file, String path) {
        String md5 = Utils.getMD5ByFile(file);
        path = path + Utils.getPathByMD5(md5) + "/";
        if ("1".equals(SystemConfig.getProperty("upload.mode"))) {
            return uploadLocal(file, path);
        } else {
            return ueUploadFtp(file, path);
        }
    }

    /**
     * @param file
     * @param path 注意：本路径请以符号 / 开头，符号 / 结尾
     * @return
     */
    private static String uploadLocal(MultipartFile file, String path) {
        if (file != null && !file.isEmpty()) {
            try {
                String fileSuffix = "." + FilenameUtils.getExtension(file.getOriginalFilename());
                String id = Utils.getuuid();
                String rootPath = getPath() + "/upload";
                String filePath = path + id + fileSuffix;
                Utils.hasfolder(rootPath + path);
                file.transferTo(new File(rootPath, filePath));
                return "/upload" + filePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static class SaveFtpFile implements Runnable {
        private FConfig fConfig;
        private String ftpPath;
        private String fileName;
        private InputStream inputStream;
        private String ftpUserName;
        private String ftpPassword;

        public SaveFtpFile(FConfig fConfig, String ftpPath, String fileName, String ftpUserName, String ftpPassword, InputStream inputStream) {
            this.fConfig = fConfig;
            this.ftpPath = ftpPath;
            this.fileName = fileName;
            this.inputStream = inputStream;
            this.ftpUserName = ftpUserName;
            this.ftpPassword = ftpPassword;
        }

        @Override
        public void run() {
            FtpUtil.uploadFile(fConfig.getFtpHost(), ftpUserName, ftpPassword, fConfig.getFtpPort(), ftpPath, fileName, inputStream);
        }
    }

    private static class SaveFtpFileCourse implements Runnable {
        private FConfig fConfig;
        private String ftpPath;
        private String localhost;
        private String ftpUserName;
        private String ftpPassword;

        public SaveFtpFileCourse(FConfig fConfig, String ftpPath, String localhost, String ftpUserName, String ftpPassword) {
            this.fConfig = fConfig;
            this.ftpPath = ftpPath;
            this.localhost = localhost;
            this.ftpUserName = ftpUserName;
            this.ftpPassword = ftpPassword;
        }

        @Override
        public void run() {
            FTPClient ftpClient = FtpUtil.getFTPClient(fConfig.getFtpHost(), ftpUserName, ftpPassword, fConfig.getFtpPort());
            FtpUtil.uploadDirectory(ftpClient, ftpPath, localhost, ftpPath, 0);
            FtpUtil.ftpLogOut(ftpClient);
        }
    }

    private static String uploadFtp(MultipartFile file, String path) {
        if (file != null && !file.isEmpty()) {
            List<FConfig> fConfigs = getFConfig();
            return uploadF(file, path, fConfigs, true);
        }
        return null;
    }

    private static String ueUploadFtp(MultipartFile file, String path) {
        if (file != null && !file.isEmpty()) {
            List<FConfig> fConfigs = getFConfig();
            return uploadF(file, path, fConfigs, false);
        }
        return null;
    }

    /**
     * 上传scorm课程文件夹
     *
     * @param path
     * @return
     */
    public static String uploadFtpCourse(String path) {
        List<FConfig> fConfigs = getFConfig();
        return upload(path, fConfigs);
    }

    // 课程上传
    private static String upload(String filePath, List<FConfig> fConfigs) {
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        String rootPath = SystemConfig.getProperty("spring.ftp.ftpPath");
        String ftpUserName = SystemConfig.getProperty("spring.ftp.ftpUserName");
        String ftpPassword = SystemConfig.getProperty("spring.ftp.ftpPassword");
        String ftpPath = rootPath + filePath;
        for (FConfig config : fConfigs) {
            new Thread(new SaveFtpFileCourse(config, ftpPath, getPath() + "/" + filePath, ftpUserName, ftpPassword)).start();
        }
        return "/" + rootPath + filePath;
    }

    /**
     * @param mFile
     * @param path
     * @param fConfigs
     * @param syn      true-异步 false-同步
     * @return
     */
    private static String uploadF(MultipartFile mFile, String path, List<FConfig> fConfigs, boolean syn) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String fileSuffix = "." + FilenameUtils.getExtension(mFile.getOriginalFilename());
        String id = Utils.getuuid();
        String filePath = path + id + fileSuffix;
        String fileName = id + fileSuffix;
        String rootPath = SystemConfig.getProperty("spring.ftp.ftpPath");
        String ftpUserName = SystemConfig.getProperty("spring.ftp.ftpUserName");
        String ftpPassword = SystemConfig.getProperty("spring.ftp.ftpPassword");
        try {
            String ftpPath = rootPath + path;
            for (FConfig config : fConfigs) {
                if (syn) {
                    new Thread(new SaveFtpFile(config, ftpPath, fileName, ftpUserName, ftpPassword, mFile.getInputStream())).start();
                } else {
                    FtpUtil.uploadFile(config.getFtpHost(), ftpUserName, ftpPassword, config.getFtpPort(), ftpPath, fileName, mFile.getInputStream());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "/" + rootPath + filePath;
    }

    /**
     * 获取static目录
     *
     * @return 项目路径
     */
    public static String getPath() {
        File path = null;
        try {
            if (StringUtils.isNotBlank(SystemConfig.getProperty("upload.path"))) {
                path = new File(ResourceUtils.getURL("file:" + SystemConfig.getProperty("upload.path")).getPath());
            } else {
                logger.error("upload.path信息未正确配置");
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!path.exists()) path.mkdirs();
        logger.debug("uploadPath:" + path.getAbsolutePath());
        return path.getAbsolutePath().replaceAll("\\\\", "/");
    }

    public static List<FConfig> getFConfig() {
        List<FConfig> list = Lists.newArrayList();
        String[] h = SystemConfig.getProperty("spring.ftp.ftpHost").split(",");
        String[] po = SystemConfig.getProperty("spring.ftp.ftpPort").split(",");
        for (int i = 0; i < h.length; i++) {
            FConfig fConfig = new FConfig();
            fConfig.setFtpHost(h[i]);
            fConfig.setFtpPort(Integer.parseInt(po[i]));
            list.add(fConfig);
        }
        return list;
    }

    public static MultipartFile fileToMultipartFile(File f) throws IOException {
        FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(f.toPath()), false, f.getName(), (int) f.length(), f.getParentFile());
        try (InputStream input = new FileInputStream(f); OutputStream os = fileItem.getOutputStream();) {
            IOUtils.copy(input, os);
            return new CommonsMultipartFile(fileItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取外网地址
     *
     * @param url
     * @return
     */
    public static String getExtranetUrl(String url) {
        String u1 = SystemConfig.getProperty("fileDomainName");
        String u2 = SystemConfig.getProperty("fileDomainNamePort");
        String u3 = SystemConfig.getProperty("httpinfo");
        return u3 + "://" + u1 + ":" + u2 + "/" + url;
    }

    /**
     * 获取内网地址
     *
     * @param url
     * @return
     */
    public static String getIntranetUrl(String url) {
        String u1 = SystemConfig.getProperty("spring.ftp.ftpHost");
        String u2 = SystemConfig.getProperty("spring.ftp.ftpPath.port");
        String u3 = SystemConfig.getProperty("httpinfo");
        return u3 + "://" + u1 + ":" + u2 + "/" + url;
    }

    /**
     * 分片文件上传,文件默认存储在temp/uuid文件夹中
     *
     * @param request
     */
    public static void sliceFileupload(HttpServletRequest request) {
        MultipartHttpServletRequest mQuest = (MultipartHttpServletRequest) request;
        MultipartFile file = mQuest.getFile("file");// 获得上传的文件
        String uuid = request.getParameter("uuid");// 上传站点标识，用于区分各站点，以便判断文件合并信息
        if (uuid.indexOf(".") != -1 || uuid.indexOf("/") != -1) {
            return;// 防止遍历攻击
        }
        String pathuuid = UpLoadUtils.getPath() + "/temp/" + uuid + "/";
        JSONObject fileMd5 = JSONObject.parseObject(request.getParameter("fileMd5"));// 获取文件md5
        String chunkStr = request.getParameter("chunk");
        String chunksStr = request.getParameter("chunks");
        if (chunkStr == null) {
            chunkStr = "0";
        }
        if (chunksStr == null) {
            chunksStr = "1";
        }
        Integer chunk = Integer.valueOf(chunkStr);// 第几片
        Integer chunks = Integer.valueOf(chunksStr);// 一共分了几片
        String fileid = request.getParameter("id");// 文件id（文件分片上传的时候，文件id是一样的，同一个站点不会有相同文件id，但是不同站点的id可能会相同）
        logger.info("uuid:{},chunk:{},chunks{}", uuid, chunk, chunks);
        if (!file.isEmpty()) {
            String oldfilename = file.getOriginalFilename();
            String suffix = FilenameUtils.getExtension(oldfilename);
            try {
                Utils.hasfolder(pathuuid);
                File uploadFile = new File(pathuuid + chunk + fileid + "." + suffix);
                FileCopyUtils.copy(file.getBytes(), uploadFile);
                Set<File> files = Sets.newConcurrentHashSet();
                for (int i = 0; i < chunks; i++) {
                    File chunkFile = new File(pathuuid + i + fileid + "." + suffix);
                    if (chunkFile.exists()) {
                        files.add(chunkFile);
                    }
                }
                if (chunks == files.size()) {// 文件全部到齐，开始合并
                    String fFileName = fileMd5.getString(uuid + fileid) + "." + FilenameUtils.getExtension(oldfilename);
                    try {
                        logger.info("开始合并分片文件,uuid:{},fileid:{},fFileName:{}", uuid, fileid, fFileName);
                        File destFile = new File(pathuuid + fFileName);
                        OutputStream out = new FileOutputStream(destFile);
                        BufferedOutputStream bos = new BufferedOutputStream(out);
                        for (int i = 0; i < chunks; i++) {// 按顺序，从第一个开始进行拼接（不直接循环files进行拼接，不仅因为hash是无序的，传过来的顺序也是无序的，就算用hashtree存了，也不能使用files）
                            File srcFile = new File(pathuuid + i + fileid + "." + suffix);
                            InputStream in = new FileInputStream(srcFile);
                            BufferedInputStream bis = new BufferedInputStream(in);
                            byte[] bytes = new byte[1024 * 1024];
                            int len = -1;
                            while ((len = bis.read(bytes)) != -1) {
                                bos.write(bytes, 0, len);
                            }
                            bis.close();
                            in.close();
                            srcFile.delete();
                        }
                        if (bos != null) bos.close();
                        if (out != null) out.close();
                        logger.info("分片文件合并完成，fFileName：{}", fFileName);
                    } catch (Exception e) {
                        logger.info("分片文件合并出错，fFileName：{}", fFileName);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
