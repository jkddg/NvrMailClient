package com.jkddg.nvrmailclient.task;

import com.jkddg.nvrmailclient.model.CapturePool;
import com.jkddg.nvrmailclient.model.StreamFile;
import com.jkddg.nvrmailclient.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * @Author 黄永好
 * @create 2023/1/13 9:26
 */
@Slf4j
@Component
public class TimingSlowMailTask  {


    @Autowired
    private MailService mailService;
    @Scheduled(fixedRate =  5 * 60 * 1000)   //定时器定义，设置执行时间
    @Async("taskPoolExecutor")
    public void timerMailSend() {
        List<StreamFile> list = CapturePool.poll();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        mailService.sendMail(list);
    }


}
