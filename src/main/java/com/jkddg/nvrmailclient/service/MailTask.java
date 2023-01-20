package com.jkddg.nvrmailclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @Author 黄永好
 * @create 2023/1/13 9:26
 */
@Slf4j
@Component
public class MailTask {


//    @Autowired
//    MailService mailService;


//    @Scheduled(fixedRate = 5 * 1000)   //定时器定义，设置执行时间 5s
//    @Async("taskPoolExecutor")
//    public void checkMailSend() {
//        mailService.checkAndSendMail();
//
//    }

//    @Bean(name = "taskPoolExecutor")
//    public ExecutorService taskPoolExecutor() {
//        ExecutorService poolExecutor = new ThreadPoolExecutor(2, 4, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        return poolExecutor;
//    }

}
