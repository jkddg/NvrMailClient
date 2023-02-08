package com.jkddg.nvrmailclient;


import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.AlarmHelper;
import com.jkddg.nvrmailclient.hkHelper.ConfigHelper;
import com.jkddg.nvrmailclient.hkHelper.LoginHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class NvrMailClientApplication {


    public static void main(String[] args) {
        SpringApplication.run(NvrMailClientApplication.class, args);
        SDKInit.init();
        OpencvInit.init();
//        HumanBodyRecognition.test();
        int lUserID = LoginHelper.loginByConfig();
        //1、布防
        if (lUserID > -1) {
            log.info("登录成功");
            ConfigHelper.getCfg(lUserID);
            AlarmHelper.StartAlarm(lUserID);
        } else {
            log.info("登录失败");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("开始退出程序,资源回收");
                AlarmHelper.EndAlarm();
                SDKConstant.hCNetSDK.NET_DVR_Logout(SDKConstant.lUserID);
                log.info("用户退出成功");
                //释放SDK资源
                SDKConstant.hCNetSDK.NET_DVR_Cleanup();
                log.info("SDK资源回收成功");
            }
        }));
    }


}
