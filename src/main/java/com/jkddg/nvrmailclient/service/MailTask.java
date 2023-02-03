package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @Author 黄永好
 * @create 2023/1/13 9:26
 */
@Slf4j
@Component
public class MailTask {


    /**
     * 任务上次执行时间
     */
    private static LocalDateTime lastExecutionTime = null;
    @Autowired
    AlarmService alarmService;


    @Scheduled(fixedRate = 2 * 1000)   //定时器定义，设置执行时间
    @Async("taskPoolExecutor")
    public void timerMailSend() {
        if (lastExecutionTime == null) {
            lastExecutionTime = LocalDateTime.now();
            doMailSend();
            return;
        }
        if (DateUtil.chkInBetweenNow(NvrConfigConstant.daytimeStart, NvrConfigConstant.daytimeEnd)) {
            if (LocalDateTime.now().minusSeconds(NvrConfigConstant.daytimeCaptureIntervalSecond).isAfter(lastExecutionTime)) {
                lastExecutionTime = LocalDateTime.now();
                doMailSend();
            }
        } else {
            if (LocalDateTime.now().minusSeconds(NvrConfigConstant.nightCaptureIntervalSecond).isAfter(lastExecutionTime)) {
                lastExecutionTime = LocalDateTime.now();
                doMailSend();
            }
        }
    }

    private void doMailSend() {
//        log.info("MailSend");
        List<ChannelInfo> list = ChannelHelper.getOnLineIPChannels(SDKConstant.lUserID);
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> channels = list.stream().map(ChannelInfo::getNumber).collect(Collectors.toList());
            alarmService.alarmAppendQueue(channels);
        }
    }

    @Bean(name = "taskPoolExecutor")
    public ExecutorService taskPoolExecutor() {
        ExecutorService poolExecutor = new ThreadPoolExecutor(2, 4, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        return poolExecutor;
    }

}
