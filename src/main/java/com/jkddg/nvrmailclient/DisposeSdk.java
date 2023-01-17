package com.jkddg.nvrmailclient;

import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.AlarmHelper;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

/**
 * @Author 黄永好
 * @create 2023/1/17 9:50
 */
@Service
public class DisposeSdk {
    @PreDestroy
    public void preDestroy() {
//        log.info("调用preDestroy资源回收");
        AlarmHelper.EndAlarm();
        SDKConstant.hCNetSDK.NET_DVR_Logout(SDKConstant.lUserID);
        //释放SDK资源
        SDKConstant.hCNetSDK.NET_DVR_Cleanup();
    }

}
