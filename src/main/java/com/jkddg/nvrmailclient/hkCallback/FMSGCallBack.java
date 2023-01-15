package com.jkddg.nvrmailclient.hkCallback;


import com.jkddg.nvrmailclient.HCNetSDK;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Component;


/**
 * @author jiangxin
 * @create 2022-08-15-17:26
 */
@Component
public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
    static AlarmProcess alarmProcess = new AlarmProcess();

    //报警信息回调函数
    public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
//        AlarmDataParse.alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        alarmProcess.process(lCommand, pAlarmer, pAlarmInfo);
    }
}
