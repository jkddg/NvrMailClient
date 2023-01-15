
package com.jkddg.nvrmailclient.hkCallback;


import com.jkddg.nvrmailclient.HCNetSDK;
import com.sun.jna.Pointer;


public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {
    static AlarmProcess alarmProcess = new AlarmProcess();

    //报警信息回调函数
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
//        AlarmDataParse.alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        alarmProcess.process(lCommand, pAlarmer, pAlarmInfo);
        return true;
    }
}







