package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.CapturePictureHelper;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.hkHelper.LoginHelper;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.StreamFile;
import com.jkddg.nvrmailclient.service.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class MailCaptureService {

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    public static LinkedBlockingQueue<StreamFile> CAPTURE_QUEUE = new LinkedBlockingQueue<>();

    static Map<Integer, LocalDateTime> alarmTimeMap = new HashMap<>();//key=通道号，value=上次预警时间

    @Autowired
    private CapturePictureHelper capturePictureHelper;
    @Autowired
    MailService mailService;

    public void alarmCapture(List<Integer> channels) {
        for (Integer channel : channels) {
            for (int i = 0; i < NvrConfigConstant.alarmCaptureCount; i++) {
                int captureIndex = i + 1;
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        appendCapture(channel, captureIndex);
                    }
                }, NvrConfigConstant.alarmIntervalSecond * i, TimeUnit.SECONDS);
            }
        }
    }
    public void scheduleCapture(List<Integer> channels) {
        for (Integer channel : channels) {
            for (int i = 0; i < NvrConfigConstant.alarmCaptureCount; i++) {
                int captureIndex = i + 1;
                appendCapture(channel, captureIndex);
//                executor.submit(new Runnable() {
//                    @Override
//                    public void run() {
//                        appendCapture(channel, captureIndex);
//                    }
//                });
            }
        }
        mailService.sendAlarmMailNoShake();
    }

    public void appendCapture(Integer channel, int captureIndex) {
        if (SDKConstant.lUserID == -1) {
            LoginHelper.loginByConfig();
        }
        if (SDKConstant.lUserID == -1) {
            log.warn("用户未登录，结束接收抓图信息");
            return;
        }
        //判断预警间隔，太短的丢弃
        if (!alarmTimeMap.containsKey(channel)) {
            alarmTimeMap.put(channel, LocalDateTime.now());
        } else {
            LocalDateTime lastTime = alarmTimeMap.get(channel);
            LocalDateTime compareTime = LocalDateTime.now().minusSeconds(NvrConfigConstant.alarmIntervalSecond);
            if (lastTime.isAfter(compareTime)) {
//                            log.info("通道名：" + channelInfo.getName() + "，通道号：" + channel + "预警间隔不够" + NvrConfigConstant.alarmIntervalSecond + "秒，丢弃");
                return;
            }
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
        if (streamFile != null) {
//            if (NvrConfigConstant.findPeople) {
//                byte[] resByte = HumanBodyRecognition.findPeople(streamFile.getDataByte());
//                if (resByte != null) {
//                    streamFile.setDataByte(resByte);
//                    streamFile.setIdentifiedPeople(true);
//                }
//            }
            CAPTURE_QUEUE.add(streamFile);
        } else {
            log.warn(channelInfo.getName() + "第" + captureIndex + "次内存抓图失败");
        }
        alarmTimeMap.put(channel, LocalDateTime.now());
//        if (NvrConfigConstant.alarmCaptureCount == captureIndex) {
//            mailService.sendAlarmMailNoShake();
//        }
    }

}
