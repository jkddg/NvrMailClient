package com.jkddg.nvrmailclient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author 黄永好
 * @create 2023/1/10 14:23
 */
@Component
public class MainService {
    @Autowired
    private CapturePictureService capturePictureService;
    @Autowired
    private ListenAlarmService listenAlarmService;

    @Autowired
    private SendMailService sendMailService;

    public void captureAndSend(){

    }
}
