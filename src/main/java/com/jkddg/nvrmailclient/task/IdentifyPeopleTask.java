package com.jkddg.nvrmailclient.task;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.CapturePictureHelper;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.StreamFile;
import com.jkddg.nvrmailclient.opencv.HumanBodyRecognition;
import com.jkddg.nvrmailclient.service.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author 黄永好
 * @create 2023/2/11 11:08
 */
@Slf4j
@Component
public class IdentifyPeopleTask {

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    @Autowired
    private CapturePictureHelper capturePictureHelper;
    @Autowired
    private MailService mailService;

    private LocalTime dayTimeStart=LocalTime.of(7,30);
    private LocalTime dayTimeEnd=LocalTime.of(17,30);

    @Scheduled(fixedDelay = 6 * 1000)   //定时器定义，设置执行时间
    public void identifyMailSend() {
        if (NvrConfigConstant.findPeople && LocalTime.now().isAfter(dayTimeStart) && LocalTime.now().isBefore(dayTimeEnd)) {
            List<ChannelInfo> channelInfos = ChannelHelper.getOnLineIPChannels(SDKConstant.lUserID);
            for (int i = 0; i < channelInfos.size(); i++) {
                ChannelInfo channelInfo = channelInfos.get(i);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        log.info("通道[" + channelInfo.getName() + "]触发抓图事件");
                        //2、通道截图
                        String picPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                        //内存抓图
                        StreamFile streamFile = capturePictureHelper.getMemoryImage(picPrefix, channelInfo);
                        if (streamFile != null) {
                            byte[] resByte = HumanBodyRecognition.findPeople(streamFile.getDataByte());
                            if (resByte != null) {
                                streamFile.setDataByte(resByte);
                                streamFile.setIdentifiedPeople(true);
                                List<StreamFile> streamFiles = new ArrayList<>();
                                streamFiles.add(streamFile);
                                mailService.sendMail(streamFiles, "【有人】");
                            }
                        }
                    }
                }, i * 1200, TimeUnit.MILLISECONDS);
            }
        }
    }
}
