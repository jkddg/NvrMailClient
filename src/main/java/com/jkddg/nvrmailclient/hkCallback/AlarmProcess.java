package com.jkddg.nvrmailclient.hkCallback;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.service.AlarmService;
import com.jkddg.nvrmailclient.util.ByteUtil;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author 黄永好
 * @create 2023/1/12 9:11
 */
@Slf4j
@Component
public class AlarmProcess {

    @Autowired
    AlarmService alarmService;

    public List<Integer> filterAlarmChannel(HCNetSDK.NET_DVR_ALARMINFO_V30 struAlarmInfo) {
        List<Integer> alarmChannels = new ArrayList<>();
        int[] channels = ByteUtil.byteArrayToIntArray(struAlarmInfo.byChannel);
        for (int i = 0; i < channels.length; i++) {
            if (channels[i] == 1) {
                int alarmChannel = i + 1;
                alarmChannels.add(alarmChannel);
            }
        }
        return alarmChannels;
    }

    public void process(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo) {
//        log.info("录像机回调类型编码：" + lCommand);
        switch (lCommand) {
            case HCNetSDK.COMM_ALARM_V30:  //移动侦测、视频丢失、遮挡、IO信号量等报警信息(V3.0以上版本支持的设备)
                HCNetSDK.NET_DVR_ALARMINFO_V30 struAlarmInfo = new HCNetSDK.NET_DVR_ALARMINFO_V30();
                struAlarmInfo.write();
                Pointer pAlarmInfo_V30 = struAlarmInfo.getPointer();
                pAlarmInfo_V30.write(0, pAlarmInfo.getByteArray(0, struAlarmInfo.size()), 0, struAlarmInfo.size());
                struAlarmInfo.read();
//                log.info("报警类型：" + struAlarmInfo.dwAlarmType);  // 3-移动侦测
                List<Integer> channel = filterAlarmChannel(struAlarmInfo);
                alarmService.alarmAppendQueue(channel);
                break;
            case HCNetSDK.COMM_ALARM_V40: //移动侦测、视频丢失、遮挡、IO信号量等报警信息，报警数据为可变长
                log.warn("未处理的移动侦测类型COMM_ALARM_V40");
//                HCNetSDK.NET_DVR_ALARMINFO_V40 struAlarmInfoV40 = new HCNetSDK.NET_DVR_ALARMINFO_V40();
//                struAlarmInfoV40.write();
//                Pointer pAlarmInfoV40 = struAlarmInfoV40.getPointer();
//                pAlarmInfoV40.write(0, pAlarmInfo.getByteArray(0, struAlarmInfoV40.size()), 0, struAlarmInfoV40.size());
//                struAlarmInfoV40.read();
//                log.info("报警类型:" + struAlarmInfoV40.struAlarmFixedHeader.dwAlarmType); //3-移动侦测
                break;
        }

    }
}
