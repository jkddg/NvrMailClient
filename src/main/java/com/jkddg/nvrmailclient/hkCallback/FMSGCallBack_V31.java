
package com.jkddg.nvrmailclient.hkCallback;


import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.util.SpringUtil;
import com.sun.jna.Pointer;


public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {

    private AlarmProcess alarmProcess = null;

    //报警信息回调函数
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
//        AlarmDataParse.alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        if (alarmProcess == null) {
            alarmProcess = SpringUtil.getBean(AlarmProcess.class);
        }
        alarmProcess.process(lCommand, pAlarmer, pAlarmInfo);
        return true;
    }
}







