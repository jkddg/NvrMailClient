package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.CapturePictureHelper;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.hkHelper.LoginHelper;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.StreamFile;
import com.jkddg.nvrmailclient.service.mail.MailService;
import com.jkddg.nvrmailclient.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author 黄永好
 * @create 2023/1/12 9:38
 */
@Slf4j
@Component
public class FtpCaptureService {

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    @Autowired
    private CapturePictureHelper capturePictureHelper;
    @Autowired
    FtpService ftpService;

    public void alarmCapture(List<Integer> channels) {
        for (Integer channel : channels) {
            for (int i = 0; i < NvrConfigConstant.alarmCaptureCount; i++) {
                int captureIndex = i + 1;
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        doCapture(channel, captureIndex);
                    }
                }, NvrConfigConstant.alarmIntervalSecond * i, TimeUnit.SECONDS);
            }
        }
    }

    public void scheduleCapture(List<Integer> channels) {
        for (Integer channel : channels) {
            for (int i = 0; i < NvrConfigConstant.alarmCaptureCount; i++) {
                int captureIndex = i + 1;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        doCapture(channel, captureIndex);
                    }
                });
            }
        }
    }

    public void doCapture(Integer channel, int captureIndex) {
        if (SDKConstant.lUserID == -1) {
            LoginHelper.loginByConfig();
        }
        if (SDKConstant.lUserID == -1) {
            log.warn("用户未登录，结束接收抓图信息");
            return;
        }

        //1、判断通道是否在线
        ChannelInfo channelInfo = ChannelHelper.getOnlineChannelInfoByNo(channel);
        if (channelInfo == null) {
            log.warn("通道不在线，通道号：" + channel);
            ChannelHelper.flashChannel();
            return;
        }
        log.info("通道[" + channelInfo.getName() + "]触发抓图事件");
        //2、通道截图
        String picPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss-"));
        //内存抓图
        StreamFile streamFile = capturePictureHelper.getMemoryImage(picPrefix + captureIndex, channelInfo);
        ftpService.uploadFile(DateUtil.localDate2Str(LocalDate.now()), streamFile.getFileName(), new ByteArrayInputStream(streamFile.getDataByte()),false);
    }

}
