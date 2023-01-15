package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import lombok.extern.slf4j.Slf4j;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;
import static com.jkddg.nvrmailclient.constant.SDKConstant.lUserID;

/**
 * @Author 黄永好
 * @create 2023/1/11 16:07
 */
@Slf4j
public class LoginHelper {

    public static int loginByConfig() {
        return login_V40(NvrConfigConstant.serverIp, (short) NvrConfigConstant.serverPort, NvrConfigConstant.serverUser, NvrConfigConstant.serverPwd);
    }

    /**
     * 设备登录V40 与V30功能一致
     *
     * @param ip   设备IP
     * @param port SDK端口，默认设备的8000端口
     * @param user 设备用户名
     * @param psw  设备密码
     */
    public static int login_V40(String ip, short port, String user, String psw) {
        //注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        String m_sDeviceIP = ip;//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = user;//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = psw;//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = port;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.byLoginMode = 2;  //ISAPI登录
        m_strLoginInfo.write();

        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID == -1) {
            log.info("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return -1;
        } else {
            log.info(ip + ":设备登录成功！");
            //相机一般只有一个通道号，热成像相机有2个通道号，通道号为1或1,2
            //byStartDChan为IP通道起始通道号, 预览回放NVR的IP通道时需要根据起始通道号进行取值
//            if ((int) m_strDeviceInfo.struDeviceV30.byStartChan == 1 && (int) m_strDeviceInfo.struDeviceV30.byStartDChan == 33) {
//                //byStartDChan为IP通道起始通道号, 预览回放NVR的IP通道时需要根据起始通道号进行取值,NVR起始通道号一般是33或者1开始
//                lDChannel = (int) m_strDeviceInfo.struDeviceV30.byStartDChan;
//                log.info("预览起始通道号：" + lDChannel);
//            }
//            SDKHelper.getIPChannelInfo(lUserID);
            return lUserID;
        }
    }

    /**
     * 设备撤防，设备注销
     *
     * @param
     */
    public static void logout() {
        if (lUserID > -1) {
            if (hCNetSDK.NET_DVR_Logout(lUserID)) {
                log.info("注销成功");
            }
        }


        return;
    }
}
