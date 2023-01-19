package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;
import static com.jkddg.nvrmailclient.constant.SDKConstant.lUserID;

/**
 * @Author 黄永好
 * @create 2023/1/11 16:06
 */
@Slf4j
public class ChannelHelper {

    public static List<ChannelInfo> channelInfos = null;
    private static LocalDateTime initChannelTime = null;
    private static Object flashLockObj = new Object();

    /**
     * 获取IP通道及状态
     *
     * @param iUserID
     */
    public static List<ChannelInfo> getIPChannelInfo(int iUserID) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_IPPARACFG_V40 m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG_V40();
        m_strIpparaCfg.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, 0, lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
        m_strIpparaCfg.read();
        log.info("起始数字通道号：" + m_strIpparaCfg.dwStartDChan);

        for (int iChannum = 0; iChannum < m_strIpparaCfg.dwDChanNum; iChannum++) {
            ChannelInfo channelInfo = new ChannelInfo();
            int channum = iChannum + m_strIpparaCfg.dwStartDChan;
            channelInfo.setNumber(channum);
            HCNetSDK.NET_DVR_PICCFG_V40 strPicCfg = new HCNetSDK.NET_DVR_PICCFG_V40();
            strPicCfg.dwSize = strPicCfg.size();
            strPicCfg.write();
            Pointer pStrPicCfg = strPicCfg.getPointer();
            NativeLong lChannel = new NativeLong(channum);
            IntByReference pInt = new IntByReference(0);
            boolean b_GetPicCfg = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_PICCFG_V40, lChannel.intValue(), pStrPicCfg, strPicCfg.size(), pInt);
            if (b_GetPicCfg == false) {
                log.info("获取图像参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            }
            strPicCfg.read();
            m_strIpparaCfg.struStreamMode[iChannum].read();
            if (m_strIpparaCfg.struStreamMode[iChannum].byGetStreamType == 0) {
                m_strIpparaCfg.struStreamMode[iChannum].uGetStream.setType(HCNetSDK.NET_DVR_IPCHANINFO.class);
                m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.read();

                log.info("--------------第" + (iChannum + 1) + "个通道------------------");
                int channel = m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.byIPID + m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.byIPIDHigh * 256;
                log.info("channel:" + channel);
                if (channel > 0) {
                    channelInfo.setIp(new String(m_strIpparaCfg.struIPDevInfo[channel - 1].struIP.sIpV4).trim());
                    log.info("ip： " + new String(m_strIpparaCfg.struIPDevInfo[channel - 1].struIP.sIpV4).trim());
                }
                try {
                    channelInfo.setName(new String(strPicCfg.sChanName, "GBK").trim());
                    log.info("name： " + new String(strPicCfg.sChanName, "GBK").trim());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                if (m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.byEnable == 1) {
                    log.info("IP通道" + channum + "在线");
                    channelInfo.setOnLine(true);
                } else {
                    channelInfo.setOnLine(false);
                    log.info("IP通道" + channum + "不在线");

                }
            }
            channelInfos.add(channelInfo);
        }
        return channelInfos;
    }


    public static ChannelInfo getOnlineChannelInfoByNo(int channel) {
        List<ChannelInfo> channelInfoList = getOnLineIPChannels(lUserID);
        if (!CollectionUtils.isEmpty(channelInfoList)) {
            for (ChannelInfo channelInfo : channelInfoList) {
                if (channelInfo.getNumber() == channel) {
                    return channelInfo;
                }
            }
        }
        return null;
    }

    public static List<ChannelInfo> getOnLineIPChannels(int iUserID) {
        if (CollectionUtils.isEmpty(channelInfos)) {
            initChannel(iUserID);
        }
        flashChannel();
        return channelInfos.stream().filter(p -> p.isOnLine()).collect(Collectors.toList());
    }

    public static void initChannel(int iUserID) {
        log.info("加载通道-UserId=" + lUserID + ",time=" + LocalDateTime.now());
        if (iUserID == -1) {
            return;
        }
        channelInfos = getIPChannelInfo(iUserID);
    }

    /**
     * 一分钟刷新一次在线通道信息
     */
    public static void flashChannel() {
//        log.info("刷新通道-UserId=" + lUserID + ",time=" + LocalDateTime.now());
        if (initChannelTime == null || initChannelTime.isBefore(LocalDateTime.now().minusMinutes(NvrConfigConstant.channelFlashMinute))) {
            synchronized (flashLockObj) {
                if (lUserID == -1) {
                    return;
                }
                if (initChannelTime == null || initChannelTime.isBefore(LocalDateTime.now().minusMinutes(NvrConfigConstant.channelFlashMinute))) {
                    initChannel(lUserID);
                    initChannelTime = LocalDateTime.now();
                    log.info("刷新通道完成-UserId=" + lUserID + ",time=" + LocalDateTime.now());
                }
            }
        }
    }

}
