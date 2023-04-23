package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.time.LocalDate;

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
    public static void initFtpClient() {
        if (ftpClient == null || !ftpClient.isAvailable()) {
            synchronized (ftpLock) {
                if (ftpClient == null || !ftpClient.isAvailable()) {
                    ftpClient = new FTPClient();
                    try {
                        log.info("connecting...ftp服务器:" + NvrConfigConstant.ftpHost + ":" + NvrConfigConstant.ftpPort);
                        ftpClient.connect(NvrConfigConstant.ftpHost, NvrConfigConstant.ftpPort); //连接ftp服务器
                        ftpClient.login(NvrConfigConstant.ftpUser, NvrConfigConstant.ftpPassword); //登录ftp服务器

                        ftpClient.enterLocalPassiveMode();
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        ftpClient.setControlEncoding("utf-8");

                        int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
                        if (FTPReply.isPositiveCompletion(replyCode)) {
                            log.info("connect success...ftp服务器:" + NvrConfigConstant.ftpHost + ":" + NvrConfigConstant.ftpPort);
                        } else {
                            log.info("connect failed...ftp服务器:" + NvrConfigConstant.ftpHost + ":" + NvrConfigConstant.ftpPort);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
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
    public static boolean uploadFile(String pathname, String fileName, File file) {
        boolean flag = false;
        InputStream inputStream = null;
        try {
            log.info("开始上传文件");
            //把文件转化为流
            inputStream = new FileInputStream(file);
            //初始化ftp
            initFtpClient();
            //设置编码
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //文件需要保存的路径
            createDirecroty(pathname);
//            ftpClient.makeDirectory(pathname);
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            log.info("上传文件成功");

        } catch (Exception e) {
            log.info("上传文件失败");
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
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
     * @param pathname    ftp服务保存地址
     * @param fileName    上传到ftp的文件名
     * @param inputStream 输入文件流
     * @return
     */
    public boolean uploadFile(String pathname, String fileName, InputStream inputStream){
        return uploadFile(pathname,fileName,inputStream,true);
    }
    public boolean uploadFile(String pathname, String fileName, InputStream inputStream,boolean logout) {
        boolean flag = false;
        try {
            log.info("开始上传文件");
            initFtpClient();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            createDirecroty(pathname);
//            ftpClient.makeDirectory(pathname);
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.storeFile(encodeFtpFileName(fileName), inputStream);
            inputStream.close();
            if(logout) {
                ftpClient.logout();
            }
            flag = true;
            log.info("上传文件成功");
        } catch (Exception e) {
            log.info("上传文件失败");
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static void logout(){
        try {
            ftpClient.logout();
        } catch (IOException e) {
            log.error("ftp退出失败");
        }finally {
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
    public static boolean changeWorkingDirectory(String directory) {
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
    public static boolean createDirecroty(String remote) throws IOException {
        boolean success = true;
        String directory = remote + "/";
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(new String(directory))) {
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
            while (true) {
                String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
                path = path + "/" + subDirectory;
                if (!existFile(path)) {
                    if (makeDirectory(subDirectory)) {
                        changeWorkingDirectory(subDirectory);
                    } else {
                        log.info("创建目录[" + subDirectory + "]失败");
                        changeWorkingDirectory(subDirectory);
                    }
                } else {
                    changeWorkingDirectory(subDirectory);
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

    //判断ftp服务器文件是否存在
    public static boolean existFile(String path) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    //创建目录
    public static boolean makeDirectory(String dir) {
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            if (flag) {
                log.info("创建文件夹" + dir + " 成功！");

            } else {
                log.info("创建文件夹" + dir + " 失败！");
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
    public static boolean downloadFile(String pathname, String filename, String localpath) {
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
      return new String(name.getBytes("GBK"),"iso-8859-1");
    }

    private String decodeFtpFileName(String name) throws UnsupportedEncodingException {
        return  new String(name.getBytes("iso-8859-1"), "GBK");
    }

    public void test() {
        String[] path = new String[]{"前门-20230214120702.jpg","前门-20230213115346.jpg", "前门-20230212113041.jpg", "前门-20230211175819.jpg", "内院-20230211173923.jpg", "前门-20230211173509.jpg", "前门-20230211171659.jpg", "前门-20230211165342.jpg", "前门-20230211165606.jpg", "内院-20230211105657-1.jpg", "内院-20230211103714-1.jpg", "20230206095459-1.jpg", "2020011813492339.jpg", "内院-20230210123330-1.jpg", "前门-20230211101215-1.jpg", "前门-20230211100603-1.jpg", "内院-20230211102133-1.jpg", "内院-20230211102643-1.jpg"};
        for (String s : path) {
            File file = new File("D:\\human\\" + s);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                uploadFile(DateUtil.localDate2Str(LocalDate.now()),s,fileInputStream);
                fileInputStream.close();

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

    }

}