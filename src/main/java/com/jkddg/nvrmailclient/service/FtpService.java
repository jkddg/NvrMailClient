package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * @Author 黄永好
 * @create 2023/4/23 10:09
 */
@Slf4j
@Component
public class FtpService {

    private static Object ftpLock = new Object();
    private static FTPClient ftpClient = null;

    /**
     * 初始化ftp服务器
     */
    public void initFtpClient() {

        if (ftpClient == null || !ftpClient.isConnected() || !ftpClient.isAvailable()) {
            if (ftpClient == null) {
                log.info("ftpClient=null");
            } else {
                log.info("ftpClient.isConnected=" + ftpClient.isConnected() + ",ftpClient.isAvailable=" + ftpClient.isAvailable());
            }
            synchronized (ftpLock) {
                if (ftpClient == null || !ftpClient.isConnected() || !ftpClient.isAvailable()) {
                    ftpClient = new FTPClient();
                    ftpClient.setConnectTimeout(10000);
                    ftpClient.setDefaultTimeout(10000);
                    ftpClient.setDataTimeout(10000);
//                    ftpClient.setControlEncoding("utf-8");
                    try {
                        log.info("connecting...ftp服务器:" + NvrConfigConstant.ftpHost + ":" + NvrConfigConstant.ftpPort);
                        ftpClient.connect(NvrConfigConstant.ftpHost, NvrConfigConstant.ftpPort); //连接ftp服务器
                        ftpClient.login(NvrConfigConstant.ftpUser, NvrConfigConstant.ftpPassword); //登录ftp服务器

                        ftpClient.enterLocalPassiveMode();
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);


                        int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
                        if (FTPReply.isPositiveCompletion(replyCode)) {
                            ftpClient.setSoTimeout(10000);
                            log.info("connect success...ftp服务器:" + NvrConfigConstant.ftpHost + ":" + NvrConfigConstant.ftpPort);
                        } else {
                            ftpClient.disconnect();
                            log.info("connect failed...ftp服务器:" + NvrConfigConstant.ftpHost + ":" + NvrConfigConstant.ftpPort);
                        }
                    } catch (Exception e) {
                        ftpClient = null;
                        log.error("登录FTP服务器异常,ftpClient设置为null", e);
                    }
                }
            }
        }
    }


    /**
     * 上传文件
     *
     * @param pathname ftp服务保存地址
     * @param fileName 上传到ftp的文件名
     * @param file     待上传文件的名称（绝对地址） *
     * @return
     */
    public boolean uploadFile(String pathname, String fileName, File file) {
        InputStream inputStream = null;
        try {
            log.info("开始上传文件");
            //把文件转化为流
            inputStream = new FileInputStream(file);
            //初始化ftp
            initFtpClient();
            if (ftpClient == null || !ftpClient.isAvailable()) {
                log.info("FTP未创建连接,上传文件失败");
                return false;
            }
            //设置编码
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //文件需要保存的路径
            createAndChangeDirecroty(pathname);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            log.info("上传文件成功");

        } catch (Exception e) {
            log.info("上传文件失败");
            log.error("上传文件失败", e);
            ftpClient = null;
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    log.info("退出FTP成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 上传文件
     *
     * @param pathName    ftp服务保存地址
     * @param fileName    上传到ftp的文件名
     * @param inputStream 输入文件流
     * @return
     */
    public boolean uploadFile(String pathName, String fileName, InputStream inputStream) {
        return uploadFile(pathName, fileName, inputStream, true);
    }

    public boolean uploadFile(String pathName, String fileName, InputStream inputStream, boolean logout) {
        try {
            log.info("开始上传文件");
            initFtpClient();
            if (ftpClient == null || !ftpClient.isAvailable()) {
                log.info("FTP未创建连接,上传文件失败");
                return false;
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            createAndChangeDirecroty(pathName);
            ftpClient.storeFile(encodeFtpFileName(fileName), inputStream);
            inputStream.close();
            log.info("上传文件成功");
        } catch (Exception e) {
            log.info("上传文件失败");
            log.error("上传文件失败", e);
            ftpClient = null;
        } finally {
            if (logout && ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    log.info("退出FTP成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

    public boolean uploadFiles(String pathName, Map<String, InputStream> files) {
        try {
            log.info("开始上传文件");
            initFtpClient();
            if (ftpClient == null || !ftpClient.isConnected() || !ftpClient.isAvailable()) {
                log.info("FTP未创建连接,上传文件失败");
                return false;
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            createAndChangeDirecroty(pathName);
            for (String s : files.keySet()) {
                ftpClient.storeFile(encodeFtpFileName(s), files.get(s));
                files.get(s).close();
            }
            log.info("上传文件成功");
        } catch (Exception e) {
            log.info("上传文件失败");
            log.error("上传文件失败", e);
            ftpClient = null;
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    log.info("退出FTP成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static void logout() {
        try {
            ftpClient.logout();
        } catch (IOException e) {
            log.error("ftp退出失败");
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //改变目录路径
    public boolean changeWorkingDirectory(String directory) {
        boolean flag = true;
        try {
            flag = ftpClient.changeWorkingDirectory(directory);
            if (flag) {
                log.info("进入文件夹" + directory + " 成功！");
            } else {
                log.info("进入文件夹" + directory + " 失败！开始创建文件夹");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return flag;
    }

    //创建多层目录文件，如果有ftp服务器已存在该文件，则不创建，如果无，则创建
    public boolean createAndChangeDirecroty(String remotePath) throws IOException {
        boolean success = true;
        remotePath = StringUtils.hasText(remotePath) ? remotePath : "/";
        if (!remotePath.startsWith("/")) {
            remotePath = "/" + remotePath;
        }

        String encodePath = encodeFtpPathName(remotePath);
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!remotePath.equalsIgnoreCase("/") && !changeWorkingDirectory(encodePath)) {
            makePath(encodePath);
            changeWorkingDirectory(encodePath);
        }
        return success;
    }

    //判断ftp服务器文件是否存在
    public boolean existFile(String path) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    //创建目录
    public boolean makePath(String path) {
        boolean flag = true;
        path = path.replaceAll("/+", "/");
        String[] dirs = path.split("/");
        try {
            for (String dir : dirs) {
                if (!StringUtils.hasText(dir)) {
                    dir = "/";
                    ftpClient.changeWorkingDirectory(dir);
                }
                if (!dir.equalsIgnoreCase("/")) {
                    flag = ftpClient.makeDirectory(dir);
                    if (flag) {
                        log.info("创建文件夹" + dir + " 成功！");
                    } else {
                        log.info("创建文件夹" + dir + " 失败！");
                    }
                    ftpClient.changeWorkingDirectory(dir);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 下载文件 *
     *
     * @param pathname  FTP服务器文件目录 *
     * @param filename  文件名称 *
     * @param localpath 下载后的文件路径 *
     * @return
     */
    public boolean downloadFile(String pathname, String filename, String localpath) {
        boolean flag = false;
        OutputStream os = null;
        try {
            log.info("开始下载文件,file:{}", pathname + "/" + filename);
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(pathname);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                log.info("fileName:{},size:{}", file.getName(), file.getSize());
                if (filename.equals(file.getName())) {
                    log.info("找到文件，开始下载，dir:{}", localpath + file.getName());
                    File localFile = new File(localpath + file.getName());
                    if (!localFile.getParentFile().exists()) {
                        localFile.getParentFile().mkdirs();
                    }
                    os = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), os);
                    flag = true;

                    os.close();
                    ftpClient.logout();
                    log.info("下载文件成功...");
                }
            }
            if (!flag) {
                log.info("文件不存在，filepathname:{},", pathname + "/" + filename);

            }

        } catch (Exception e) {
            log.info("下载文件失败...");
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    /**
     * 删除文件 *
     *
     * @param pathname FTP服务器保存目录 *
     * @param filename 要删除的文件名称 *
     * @return
     */
    public boolean deleteFile(String pathname, String filename) {
        boolean flag = false;
        try {
            log.info("开始删除文件");
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.dele(filename);
            ftpClient.logout();
            flag = true;
            log.info("删除文件成功");
        } catch (Exception e) {
            log.info("删除文件失败");
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    private String encodeFtpFileName(String name) throws UnsupportedEncodingException {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        return new String(name.getBytes("GBK"), "iso-8859-1");
    }

    private String encodeFtpPathName(String path) throws UnsupportedEncodingException {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        if (path.equalsIgnoreCase("/")) {
            return path;
        }
        StringBuffer sb = new StringBuffer();
        String[] directorys = path.split("/");
        for (String directory : directorys) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(encodeFtpFileName(directory));
        }
        if (!sb.substring(0, 1).equalsIgnoreCase("/")) {
            sb = sb.insert(0, "/");
        }
        return sb.toString();
    }

    private String decodeFtpFileName(String name) throws UnsupportedEncodingException {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        return new String(name.getBytes("iso-8859-1"), "GBK");
    }

    public void test() {
        String[] path = new String[]{"前门-20230214120702.jpg", "前门-20230213115346.jpg", "前门-20230212113041.jpg", "前门-20230211175819.jpg", "内院-20230211173923.jpg", "前门-20230211173509.jpg", "前门-20230211171659.jpg", "前门-20230211165342.jpg", "前门-20230211165606.jpg", "内院-20230211105657-1.jpg", "内院-20230211103714-1.jpg", "20230206095459-1.jpg", "2020011813492339.jpg", "内院-20230210123330-1.jpg", "前门-20230211101215-1.jpg", "前门-20230211100603-1.jpg", "内院-20230211102133-1.jpg", "内院-20230211102643-1.jpg"};
        for (String s : path) {
            File file = new File("D:\\human\\" + s);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                uploadFile("aa/" + DateUtil.localDate2Str(LocalDate.now()), s, fileInputStream);

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
        logout();

    }

}
