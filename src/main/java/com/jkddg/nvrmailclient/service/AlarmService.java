package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.CapturePictureHelper;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.hkHelper.LoginHelper;
import com.jkddg.nvrmailclient.model.AlarmMailInfo;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.MailAttachment;
import com.jkddg.nvrmailclient.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 黄永好
 * @create 2023/1/12 9:38
 */
@Slf4j
public class AlarmService {

    static Map<Integer, String> lockMap = new ConcurrentHashMap<>();
    public static Queue<AlarmMailInfo> ALARM_QUEUE = new LinkedBlockingQueue<>();

    static Map<Integer, LocalDateTime> alarmTimeMap = new HashMap<>();//key=通道号，value=上次预警时间

    CapturePictureHelper capturePictureHelper = new CapturePictureHelper();


    public void alarmAppendQueue(List<Integer> channels) {
        if (!CollectionUtils.isEmpty(channels)) {
            if (SDKConstant.lUserID == -1) {
                LoginHelper.loginByConfig();
            }
            if (SDKConstant.lUserID == -1) {
                log.warn("用户未登录，结束接收预警信息");
                return;
            }
            for (Integer channel : channels) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //判断预警间隔，太短的丢弃
                        if (!alarmTimeMap.containsKey(channel)) {
                            alarmTimeMap.put(channel, LocalDateTime.now());
                        } else {
                            LocalDateTime lastTime = alarmTimeMap.get(channel);
                            if (lastTime.isAfter(LocalDateTime.now().minusSeconds(NvrConfigConstant.alarmIntervalSecond))) {
//                            log.info("通道名：" + channelInfo.getName() + "，通道号：" + channel + "预警间隔不够" + NvrConfigConstant.alarmIntervalSecond + "秒，丢弃");
                                return;
                            }
                        }
                        if (!lockMap.containsKey(channel)) {
                            lockMap.put(channel, new String("nvr-lock-" + channel));
                        }
                        String lockObject = lockMap.get(channel);
                        synchronized (lockObject) {
                            //1、判断通道是否在线
                            ChannelInfo channelInfo = ChannelHelper.getOnlineChannelInfoByNo(channel);
                            if (channelInfo == null) {
                                ChannelHelper.flashChannel();
                                channelInfo = ChannelHelper.getOnlineChannelInfoByNo(channel);
                                if (channelInfo == null) {
                                    log.warn("预警通道不在线，通道号：" + channel);
                                    return;
                                }
                            }

                            //2、通道截图
                            List<String> imageAll = new ArrayList<>();
                            List<MailAttachment> attachments = new ArrayList<>();
                            String picPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss-"));
                            for (int i = 0; i < NvrConfigConstant.captureCount; i++) {
                                if (NvrConfigConstant.captureInMemory) {
                                    //内存抓图
                                    MailAttachment mailAttachment = capturePictureHelper.getMemoryImage(picPrefix + (i + 1), channelInfo);
                                    if (mailAttachment != null) {
                                        attachments.add(mailAttachment);
                                    } else {
                                        log.warn(channelInfo.getName() + "第" + (i + 1) + "次内存抓图失败");
                                    }
                                } else {
                                    //文件抓图
                                    String imagePath = capturePictureHelper.getFileImage(picPrefix + (i + 1), channelInfo);
                                    if (StringUtils.isEmpty(imagePath)) {
                                        log.warn(channelInfo.getName() + "第" + (i + 1) + "次文件抓图失败");
                                    } else {
                                        imageAll.add(imagePath);
                                    }
                                }
                                try {
                                    Thread.sleep(NvrConfigConstant.captureIntervalSecond * 1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            alarmTimeMap.put(channel, LocalDateTime.now());
                            AlarmMailInfo alarmMailInfo = new AlarmMailInfo();
                            alarmMailInfo.setChannel(channelInfo);
                            if (!CollectionUtils.isEmpty(imageAll)) {
                                alarmMailInfo.setFileImages(imageAll);
                            }
                            if (!CollectionUtils.isEmpty(attachments)) {
                                alarmMailInfo.setStreamImages(attachments);
                            }
                            if (!CollectionUtils.isEmpty(imageAll)) {
                                ALARM_QUEUE.add(alarmMailInfo);
                                MailService mailService = SpringUtil.getBean(MailService.class);
                                mailService.checkAndSendMail();
                            }
                        }
                    }
                }).start();
            }
        }
    }
}
