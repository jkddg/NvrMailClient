package com.jkddg.nvrmailclient.constant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author huangyonghao
 * @create 2023/1/11 19:45
 */
@Component
public class NvrConfigConstant {
    @Autowired
    private Environment env;
    public static String serverIp;
    public static int serverPort;
    public static String serverUser;
    public static String serverPwd;
    public static short capturePicSize;
    public static int captureCount;
    public static int captureIntervalSecond;
    public static int listenPort;
    public static String captureFolder;
    public static String captureFolderLinux;
    public static String captureFolderWin;
    public static short capturePicQuality = 0;
    public static int alarmIntervalSecond;
    public static String mailTo;
    public static String mailFrom;
    public static boolean mailSSl;
    public static int channelFlashMinute = 5;
    public static String linuxLibPath;
    public static String winLibPath;

    @PostConstruct
    public void readConfig() {

        linuxLibPath = env.getProperty("nvr.linux-lib-path");
        winLibPath = env.getProperty("nvr.win-lib-path");
        serverIp = env.getProperty("nvr.server.ip");
        serverPort = Integer.parseInt(env.getProperty("nvr.server.port"));
        serverUser = env.getProperty("nvr.server.user");
        serverPwd = env.getProperty("nvr.server.pwd");
        listenPort = Integer.parseInt(env.getProperty("nvr.listen.port"));
//        captureFolder = env.getProperty("nvr.capture.folder");
        captureFolderWin = env.getProperty("nvr.capture.folder-win");
        captureFolderLinux = env.getProperty("nvr.capture.folder-linux");
        capturePicSize = Short.parseShort(env.getProperty("nvr.capture.pic-size"));
        captureCount = Integer.parseInt(env.getProperty("nvr.capture.count"));
        alarmIntervalSecond = Integer.parseInt(env.getProperty("nvr.alarm.second"));
        mailTo = env.getProperty("nvr.mail-to");
        mailSSl = Boolean.parseBoolean(env.getProperty("jakarta.mail.ssl"));
        captureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.sleep-second"));
        channelFlashMinute = Integer.parseInt(env.getProperty("nvr.channel.flash-minute"));
        capturePicQuality = Short.parseShort(env.getProperty("nvr.capture.pic-quality"));
    }
}
