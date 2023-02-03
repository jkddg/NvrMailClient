package com.jkddg.nvrmailclient.constant;

import com.jkddg.nvrmailclient.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    public static int daytimeCaptureIntervalSecond = 60;
    /**
     * 夜晚抓图时间间隔
     */
    public static int nightCaptureIntervalSecond = 60;

    /**
     * 白天开始时间
     */
    public static LocalTime daytimeStart;
    /**
     * 白天结束时间
     */
    public static LocalTime daytimeEnd;

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
        captureCount = Integer.parseInt(env.getProperty("nvr.capture.count"));
        alarmIntervalSecond = Integer.parseInt(env.getProperty("nvr.alarm.interval-second"));
        mailIntervalSecond = Integer.parseInt(env.getProperty("nvr.alarm.mail-interval-second"));
        mailTo = env.getProperty("nvr.mail-to");
        mailSSl = Boolean.parseBoolean(env.getProperty("jakarta.mail.ssl"));
        captureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.sleep-second"));
        channelFlashMinute = Integer.parseInt(env.getProperty("nvr.channel.flash-minute"));
        capturePicQuality = Short.parseShort(env.getProperty("nvr.capture.pic-quality"));
        sdkLogWin = env.getProperty("nvr.win-sdk-log");
        sdkLogLinux = env.getProperty("nvr.linux-sdk-log");
        captureInMemory = Boolean.parseBoolean(env.getProperty("nvr.capture.in-memory"));
        daytimeCaptureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.daytime-capture-interval-second"));
        nightCaptureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.night-capture-interval-second"));
        daytimeStart = DateUtil.getTimeFromStr(env.getProperty("nvr.capture.daytime-start"));
        daytimeEnd = DateUtil.getTimeFromStr(env.getProperty("nvr.capture.daytime-end"));
    }
}
