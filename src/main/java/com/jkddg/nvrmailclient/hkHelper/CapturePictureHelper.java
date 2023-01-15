package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;
import static com.jkddg.nvrmailclient.constant.SDKConstant.lUserID;

/**
 * @Author 黄永好
 * @create 2023/1/10 14:21
 */
@Slf4j
public class CapturePictureHelper {

    public List<String> getNVRPicByConfigPath(String indexNo, List<ChannelInfo> channels) {
        String imgFolder = NvrConfigConstant.captureFolder;
        return getNVRPic(imgFolder, indexNo, channels);
//        List<String> res = new ArrayList<>();
//        for (ChannelInfo channel : channels) {
//            String path = picCutCate(imgFolder, indexNo, channel);
//            if (StringUtils.hasText(path)) {
//                res.add(path);
//            }
//        }
//        return res;
    }

    /**
     * 抓拍图片
     *
     * @param imgPath  图片路径
     * @param channels 通道
     */
    private List<String> getNVRPic(String imgPath, String indexNo, List<ChannelInfo> channels) {
//        log.info("-----------这里处理已经getNVRPic----------" + imgPath);
        File file = new File(imgPath);
        if (!file.exists()) {
            file.mkdir();
        }
        List<String> capturePicturePath = new ArrayList<>();
        if (CollectionUtils.isEmpty(channels)) {
            log.error("通道数据为空");
            return capturePicturePath;
        }
        HCNetSDK.NET_DVR_WORKSTATE deviceWork = new HCNetSDK.NET_DVR_WORKSTATE();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState(lUserID, deviceWork)) {
            // 返回Boolean值，判断是否获取设备能力
            log.error("hkSdk(抓图)-返回设备状态失败" + hCNetSDK.NET_DVR_GetLastError());
        }

        channels.forEach(channelInfo -> {
            int channelId = channelInfo.getNumber();
            String path = imgPath + channelInfo.getName() + "-" + indexNo + ".jpeg";
            //非内存直接保存
            //图片质量
            HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
            //设置图片分辨率
            jpeg.wPicSize = NvrConfigConstant.capturePicSize;
            //设置图片质量
            jpeg.wPicQuality = NvrConfigConstant.capturePicQuality;
            //需要加入通道
//            log.info("-----------这里开始封装 NET_DVR_CaptureJPEGPicture---------");
            boolean is = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, channelId, jpeg, path.getBytes());
            log.info("-----------抓图结果----------" + is);
            if (is) {
                capturePicturePath.add(path);
            } else {
                log.info("hkSdk(抓图)-抓取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError() + ",图片路径" + path);
            }
        });
        log.info("-----------处理完成截图数据----------");
//        //退出登录
//        hCNetSDK.NET_DVR_Logout(lUserID);
//        //释放SDK资源
//        hCNetSDK.NET_DVR_Cleanup();

        return capturePicturePath;
    }


    private String picCutCate(String folderPath, String indexNo, ChannelInfo channelInfo) {
        //图片质量
        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        //设置图片分辨率
        jpeg.wPicSize = NvrConfigConstant.capturePicSize;
        //设置图片质量
        jpeg.wPicQuality = NvrConfigConstant.capturePicQuality;
        IntByReference a = new IntByReference();
        //设置图片大小
        ByteBuffer jpegBuffer = ByteBuffer.allocate(1024 * 1024);
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdir();
        }

        if (channelInfo == null) {
            log.error("通道数据为空");
            return null;
        }
        String imgPath = folderPath + channelInfo.getName() + "-" + indexNo + ".jpeg";
        file = new File(imgPath);
        // 抓图到内存，单帧数据捕获并保存成JPEG存放在指定的内存空间中
        log.info("-----------这里开始封装 NET_DVR_CaptureJPEGPicture_NEW---------");
        Pointer p = new Memory(200 * 1024);
        boolean is = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(SDKConstant.lUserID, channelInfo.getNumber(), jpeg, p, 1024 * 1024, a);
        log.info("-----------这里开始图片存入内存----------" + is);
        if (is) {
            /**
             * 该方式使用内存获取 但是读取有问题无法预览
             * linux下 可能有问题
             * */
            log.info("hksdk(抓图)-结果状态值(0表示成功):" + hCNetSDK.NET_DVR_GetLastError());
            //存储到本地
            BufferedOutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
                outputStream.write(jpegBuffer.array(), 0, a.getValue());
                outputStream.flush();
                return imgPath;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            log.info("hksdk(抓图)-抓取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError());
        }
        return null;
    }


}
