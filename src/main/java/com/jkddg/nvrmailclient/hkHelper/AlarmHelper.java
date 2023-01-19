package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.hkCallback.FMSGCallBack;
import com.jkddg.nvrmailclient.hkCallback.FMSGCallBack_V31;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;

/**
 * @Author 黄永好
 * @create 2023/1/11 16:09
 */
@Slf4j
public class AlarmHelper {

    static int lAlarmHandle = -1;//报警布防句柄
    static int lListenHandle = -1;//报警监听句柄
    static FMSGCallBack_V31 fMSFCallBack_V31 = null;
    static FMSGCallBack fMSFCallBack = null;


    /**
     * 报警布防，需要登录操作
     */
    public static void StartAlarm(int lUserID) {
        //设置报警回调函数
        if (fMSFCallBack_V31 == null) {
            fMSFCallBack_V31 = new FMSGCallBack_V31();
            Pointer pUser = null;
            if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                log.info("设置回调函数失败!");
                return;
            } else {
                log.info("设置回调函数成功!");
            }
        }
        /** 设备上传的报警信息是COMM_VCA_ALARM(0x4993)类型，
         在SDK初始化之后增加调用NET_DVR_SetSDKLocalCfg(enumType为NET_DVR_LOCAL_CFG_TYPE_GENERAL)设置通用参数NET_DVR_LOCAL_GENERAL_CFG的byAlarmJsonPictureSeparate为1，
         将Json数据和图片数据分离上传，这样设置之后，报警布防回调函数里面接收到的报警信息类型为COMM_ISAPI_ALARM(0x6009)，
         报警信息结构体为NET_DVR_ALARM_ISAPI_INFO（与设备无关，SDK封装的数据结构），更便于解析。*/

        HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG struNET_DVR_LOCAL_GENERAL_CFG = new HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG();
        struNET_DVR_LOCAL_GENERAL_CFG.byExceptionCbDirectly = 0;  //0-通过线程池异常回调，1-直接异常回调给上层
        struNET_DVR_LOCAL_GENERAL_CFG.byAlarmJsonPictureSeparate = 1;   //设置JSON透传报警数据和图片分离，0-不分离，1-分离（分离后走COMM_ISAPI_ALARM回调返回）
        struNET_DVR_LOCAL_GENERAL_CFG.write();
        Pointer pStrNET_DVR_LOCAL_GENERAL_CFG = struNET_DVR_LOCAL_GENERAL_CFG.getPointer();
        hCNetSDK.NET_DVR_SetSDKLocalCfg(17, pStrNET_DVR_LOCAL_GENERAL_CFG);
        setAlarm(lUserID);//报警布防，和报警监听二选一即可
    }

    /**
     * 报警布防，需要登录操作
     */
    public static void StartListen(String ip, int port) {
        //设置报警回调函数
        if (fMSFCallBack_V31 == null) {
            fMSFCallBack_V31 = new FMSGCallBack_V31();
            Pointer pUser = null;
            if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                log.info("设置回调函数失败!");
                return;
            } else {
                log.info("设置回调函数成功!");
            }
        }
        /** 设备上传的报警信息是COMM_VCA_ALARM(0x4993)类型，
         在SDK初始化之后增加调用NET_DVR_SetSDKLocalCfg(enumType为NET_DVR_LOCAL_CFG_TYPE_GENERAL)设置通用参数NET_DVR_LOCAL_GENERAL_CFG的byAlarmJsonPictureSeparate为1，
         将Json数据和图片数据分离上传，这样设置之后，报警布防回调函数里面接收到的报警信息类型为COMM_ISAPI_ALARM(0x6009)，
         报警信息结构体为NET_DVR_ALARM_ISAPI_INFO（与设备无关，SDK封装的数据结构），更便于解析。*/

        HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG struNET_DVR_LOCAL_GENERAL_CFG = new HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG();
        struNET_DVR_LOCAL_GENERAL_CFG.byAlarmJsonPictureSeparate = 1;   //设置JSON透传报警数据和图片分离
        struNET_DVR_LOCAL_GENERAL_CFG.write();
        Pointer pStrNET_DVR_LOCAL_GENERAL_CFG = struNET_DVR_LOCAL_GENERAL_CFG.getPointer();
        hCNetSDK.NET_DVR_SetSDKLocalCfg(17, pStrNET_DVR_LOCAL_GENERAL_CFG);
        setListen(ip, (short) port);//报警监听，不需要登陆设备//报警布防，和报警监听二选一即可
    }

    /**
     * 报警布防接口
     *
     * @param
     */
    public static void setAlarm(int lUserID) {
        if (lAlarmHandle < 0)//尚未布防,需要布防
        {
            //报警布防参数设置
            HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
            m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
            m_strAlarmInfo.byLevel = 0;  //布防等级
            m_strAlarmInfo.byAlarmInfoType = 1;   // 智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
            m_strAlarmInfo.byDeployType = 0;   //布防类型：0-客户端布防，1-实时布防
            m_strAlarmInfo.write();
            lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
            log.info("lAlarmHandle: " + lAlarmHandle);
            if (lAlarmHandle == -1) {
                log.info("布防失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.info("布防成功");

            }
        } else {
            log.info("设备已经布防，请先撤防！");
        }
    }

    /**
     * 开启监听
     *
     * @param ip   监听IP
     * @param port 监听端口
     */
    public static void setListen(String ip, short port) {
        if (fMSFCallBack == null) {
            fMSFCallBack = new FMSGCallBack();
        }
        lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(ip, port, fMSFCallBack, null);
        if (lListenHandle == -1) {
            log.info("监听失败" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            log.info("监听成功");
        }
    }

    public static void EndAlarm() {
        if (lAlarmHandle > -1) {
            if (hCNetSDK.NET_DVR_CloseAlarmChan(lAlarmHandle)) {
                log.info("撤防成功");
            }
        }
    }

    public static void EndListen() {
        if (lListenHandle > -1) {
            if (hCNetSDK.NET_DVR_StopListen_V30(lListenHandle)) {
                log.info("停止监听成功");
            }
        }
    }
}
