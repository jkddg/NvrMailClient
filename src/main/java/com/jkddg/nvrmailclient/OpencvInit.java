package com.jkddg.nvrmailclient;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;

/**
 * @Author 黄永好
 * @create 2023/2/6 16:24
 */
@Slf4j
@Component
public class OpencvInit {
    public static void init(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        log.info("加载opencv成功！");
    }

}
