package com.jkddg.nvrmailclient.task;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.CustomCaptureConfig;
import com.jkddg.nvrmailclient.service.FtpCaptureService;
import com.jkddg.nvrmailclient.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author 黄永好
 * @create 2023/4/23 9:49
 */
@Slf4j
@Component
public class ScheduleFtpTask {
    @Autowired
    FtpCaptureService ftpCaptureService;
    /**
     * 任务上次执行时间
     */
    private static LocalDateTime lastExecutionTime = null;
    private static List<CustomCaptureConfig> captureConfigs;
    @Scheduled(fixedRate = 3 * 1000)   //定时器定义，设置执行时间
    public void timerFtpSend() {
        if (lastExecutionTime == null) {
            lastExecutionTime = LocalDateTime.now();
            captureConfigs = new ArrayList<>();
            if (StringUtils.hasText(NvrConfigConstant.ftpCaptureInterval)) {
                String[] strings = NvrConfigConstant.ftpCaptureInterval.split(",");
                if (strings.length > 0) {
                    for (String s : strings) {
                        String[] ss = s.split("-");
                        if (ss.length == 3) {
                            CustomCaptureConfig customCaptureConfig = new CustomCaptureConfig();
                            customCaptureConfig.setStartTime(DateUtil.getTimeFromStr(ss[0]));
                            customCaptureConfig.setEndTime(DateUtil.getTimeFromStr(ss[1]));
                            customCaptureConfig.setIntervalSecond(Integer.parseInt(ss[2]));
                            captureConfigs.add(customCaptureConfig);
                        }
                    }
                }
            }
            doFtpSend();
            return;
        }
        //如果有自定义时间段的，遍历自定义时间段，按自定义频率发送邮件
        if (!CollectionUtils.isEmpty(captureConfigs)) {
            for (CustomCaptureConfig captureConfig : captureConfigs) {
                if (DateUtil.chkInBetweenNow(captureConfig.getStartTime(), captureConfig.getEndTime())) {
                    if (LocalDateTime.now().minusSeconds(captureConfig.getIntervalSecond()).isAfter(lastExecutionTime)) {
                        lastExecutionTime = LocalDateTime.now();
                        doFtpSend();
                    }
                    return;
                }
            }
        }
        //按默认频率发送邮件
        if (LocalDateTime.now().minusSeconds(NvrConfigConstant.defaultCaptureIntervalSecond).isAfter(lastExecutionTime)) {
            lastExecutionTime = LocalDateTime.now();
            doFtpSend();
        }
    }

    private void doFtpSend() {
        List<ChannelInfo> list = ChannelHelper.getOnLineIPChannels(SDKConstant.lUserID);
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> channels = list.stream().map(ChannelInfo::getNumber).collect(Collectors.toList());
            ftpCaptureService.scheduleCapture(channels);
        }
    }

}
