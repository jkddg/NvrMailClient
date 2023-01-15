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
    public static short captureImageSize;
    public static int captureCount;
    public static int captureIntervalSecond;
    public static int listenPort;
    public static String captureFolder;
    public static String captureFolder1;
    public static int alarmIntervalSecond;
    public static String mailTo;
    public static String mailFrom;
    public static boolean mailSSl;
    public static int channelFlashMinute = 5;

    @PostConstruct
    public void readConfig() {
        serverIp = env.getProperty("nvr.server.ip");
        serverPort = Integer.parseInt(env.getProperty("nvr.server.port"));
        serverUser = env.getProperty("nvr.server.user");
        serverPwd = env.getProperty("nvr.server.pwd");
        listenPort = Integer.parseInt(env.getProperty("nvr.listen.port"));
        captureFolder = env.getProperty("nvr.capture.folder");
        captureFolder1 = env.getProperty("nvr.capture.folder1");
        captureImageSize = Short.parseShort(env.getProperty("nvr.capture.size"));
        captureCount = Integer.parseInt(env.getProperty("nvr.capture.count"));
        alarmIntervalSecond = Integer.parseInt(env.getProperty("nvr.alarm.second"));
        mailTo = env.getProperty("nvr.mail-to");
        mailSSl = Boolean.parseBoolean(env.getProperty("jakarta.mail.ssl"));
        captureIntervalSecond = Integer.parseInt(env.getProperty("nvr.capture.sleep-second"));
        channelFlashMinute = Integer.parseInt(env.getProperty("nvr.channel.flash-minute"));
    }
}
