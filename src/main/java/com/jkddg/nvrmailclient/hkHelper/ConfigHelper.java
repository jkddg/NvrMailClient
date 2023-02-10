package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.util.ByteUtil;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;

/**
 * @Author 黄永好
 * @create 2023/1/13 13:53
 */
@Slf4j
public class ConfigHelper {
    //获取设备的基本参数
    public static void getCfg(int iUserID) {
        HCNetSDK.NET_DVR_DEVICECFG_V40 m_strDeviceCfg = new HCNetSDK.NET_DVR_DEVICECFG_V40();
        m_strDeviceCfg.dwSize = m_strDeviceCfg.size();
        m_strDeviceCfg.write();
        Pointer pStrDeviceCfg = m_strDeviceCfg.getPointer();
        IntByReference pInt = new IntByReference(0);
        boolean b_GetCfg = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_DEVICECFG_V40,
                0Xffffffff, pStrDeviceCfg, m_strDeviceCfg.dwSize, pInt);
        if (b_GetCfg == false) {
            log.info("获取参数失败  错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        log.info("获取参数成功");
        m_strDeviceCfg.read();
        String nvrName = ByteUtil.byteToString(m_strDeviceCfg.sDVRName, "GBK");
        log.info("设备名称:" + nvrName + ",设备序列号：" + new String(m_strDeviceCfg.sSerialNumber));
        SDKConstant.NvrName = nvrName;
        log.info("模拟通道个数" + m_strDeviceCfg.byChanNum);
        parseVersion(m_strDeviceCfg.dwSoftwareVersion);
        parseBuildTime(m_strDeviceCfg.dwSoftwareBuildDate);
        parseDSPBuildDate(m_strDeviceCfg.dwDSPSoftwareBuildDate);

    }

    //设备版本解析
    private static void parseVersion(int version) {
        int firstVersion = (version & 0XFF << 24) >> 24;
        int secondVersion = (version & 0XFF << 16) >> 16;
        int lowVersion = version & 0XFF;

        log.info("firstVersion:" + firstVersion);
        log.info("secondVersion:" + secondVersion);
        log.info("lowVersion:" + lowVersion);
    }

    private static void parseBuildTime(int buildTime) {
        int year = ((buildTime & 0XFF << 16) >> 16) + 2000;
        int month = (buildTime & 0XFF << 8) >> 8;
        int data = buildTime & 0xFF;
        log.info("Build:" + year + "." + month + "." + data);

    }

    private static void parseDSPBuildDate(int DSPBuildDate) {
        int year = ((DSPBuildDate & 0XFF << 16) >> 16) + 2000;
        int month = (DSPBuildDate & 0XFF << 8) >> 8;
        int data = DSPBuildDate & 0xFF;
        log.info("DSPBuildDate:" + year + "." + month + "." + data);

    }
}
