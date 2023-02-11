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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @Author 黄永好
 * @create 2023/2/11 11:08
 */
@Slf4j
@Component
public class IdentifyPeopleTask {

    private Map<Integer, LocalDateTime> map = new HashMap<>();
    private int timeSpanSecond = 10;

    @Autowired
    private CapturePictureHelper capturePictureHelper;
    @Autowired
    private MailService mailService;

    @Scheduled(fixedDelay = 2 * 1000)   //定时器定义，设置执行时间
    @Async("taskPoolExecutor")
    public void identifyMailSend() {
        if (NvrConfigConstant.findPeople) {
            List<ChannelInfo> channelInfos = ChannelHelper.getOnLineIPChannels(SDKConstant.lUserID);
            List<StreamFile> streamFiles = new ArrayList<>();
            for (ChannelInfo channelInfo : channelInfos) {
                if (!map.containsKey(channelInfo.getNumber()) || (map.get(channelInfo.getNumber()).plusSeconds(timeSpanSecond)).isBefore(LocalDateTime.now())) {
                    StreamFile streamFile = this.identifyPeople(channelInfo);
                    if (streamFile != null) {
                        byte[] resByte = HumanBodyRecognition.findPeople(streamFile.getDataByte());
                        if (resByte != null) {
                            streamFile.setDataByte(resByte);
                            streamFile.setIdentifiedPeople(true);
                            streamFiles.add(streamFile);
                        }
                    }
                    map.put(channelInfo.getNumber(), LocalDateTime.now());
                }
            }
            if (!CollectionUtils.isEmpty(streamFiles)) {
                mailService.sendMail(streamFiles, "【有人】");
            }
        }
    }

    private StreamFile identifyPeople(ChannelInfo channelInfo) {
        log.info("通道[" + channelInfo.getName() + "]触发抓图事件");
        //2、通道截图
        String picPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        //内存抓图
        StreamFile streamFile = capturePictureHelper.getMemoryImage(picPrefix, channelInfo);
        return streamFile;
    }
}
