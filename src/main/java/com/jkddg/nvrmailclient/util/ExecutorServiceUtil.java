package com.jkddg.nvrmailclient.util;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author 黄永好
 * @create 2023/2/8 14:16
 */
@Component
public class ExecutorServiceUtil {
    @Bean(name = "taskPoolExecutor")
    public ExecutorService taskPoolExecutor() {
        ExecutorService poolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
        return poolExecutor;
    }
}
