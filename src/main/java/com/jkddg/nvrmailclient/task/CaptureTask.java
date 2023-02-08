package com.jkddg.nvrmailclient.task;

import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.service.CaptureImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author 黄永好
 * @create 2023/2/7 12:31
 */
@Slf4j
@Component
public class CaptureTask {

    @Autowired
    private CaptureImageService captureImageService;

    @Scheduled(fixedDelay = 3 * 1000)   //定时器定义，设置执行时间
    public void timerCapture() {
        List<ChannelInfo> list = ChannelHelper.getOnLineIPChannels(SDKConstant.lUserID);
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> channels = list.stream().map(ChannelInfo::getNumber).collect(Collectors.toList());
            captureImageService.captureToPool(channels);
        }
    }
}
