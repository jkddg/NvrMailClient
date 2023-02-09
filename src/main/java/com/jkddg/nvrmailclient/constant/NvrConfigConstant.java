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
    public static int captureScheduleCount;
    public static int alarmCaptureCount;
    public static int captureIntervalSecond;
    public static String captureFolder;
    public static String captureFolderLinux;
    public static String captureFolderWin;
    public static String sdkLogLinux;
    public static String sdkLogWin;
    public static short capturePicQuality = 0;
    public static int alarmIntervalSecond;
    public static String mailTo;
    public static String mailFrom;
    public static boolean mailSSl;
    public static int channelFlashMinute = 5;
    public static String linuxLibPath;
    public static String winLibPath;
    public static int mailIntervalSecond;
    public static boolean captureInMemory = true;

    /**
     * 白天抓图时间间隔
     */
    public static int defaultCaptureIntervalSecond = 60;
    /**
     * 自定义时间段抓图时间间隔
     */
    public static String customCaptureInterval;


    @PostConstruct
    public void readConfig() {

        linuxLibPath = env.getProperty("nvr.linux-lib-path");
        winLibPath = env.getProperty("nvr.win-lib-path");
        serverIp = env.getProperty("nvr.server.ip");
        serverPort = Integer.parseInt(env.getProperty("nvr.server.port"));
        serverUser = env.getProperty("nvr.server.user");
        serverPwd = env.getProperty("nvr.server.pwd");
        captureFolderWin = env.getProperty("nvr.capture.folder-win");
        captureFolderLinux = env.getProperty("nvr.capture.folder-linux");
        capturePicSize = Short.parseShort(env.getProperty("nvr.capture.pic-size"));
        captureScheduleCount = Integer.parseInt(env.getProperty("nvr.capture.schedule-count"));
        alarmIntervalSecond = Integer.parseInt(env.getProperty("nvr.alarm.interval-second"));
        mailIntervalSecond = Integer.parseInt(env.getProperty("nvr.alarm.mail-interval-second"));
        mailTo = env.getProperty("nvr.mail-to");
        mailSSl = Boolean.parseBoolean(env.getProperty("jakarta.mail.ssl"));
        captureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.interval-second"));
        channelFlashMinute = Integer.parseInt(env.getProperty("nvr.channel.flash-minute"));
        capturePicQuality = Short.parseShort(env.getProperty("nvr.capture.pic-quality"));
        sdkLogWin = env.getProperty("nvr.win-sdk-log");
        sdkLogLinux = env.getProperty("nvr.linux-sdk-log");
        captureInMemory = Boolean.parseBoolean(env.getProperty("nvr.capture.in-memory"));
        defaultCaptureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.default-capture-interval-second"));
        customCaptureInterval = env.getProperty("nvr.capture.custom-capture-interval");
        alarmCaptureCount = Integer.parseInt(env.getProperty("nvr.alarm.capture-count"));
    }
}
