package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.StreamFile;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;
import static com.jkddg.nvrmailclient.constant.SDKConstant.lUserID;

/**
 * @Author 黄永好
 * @create 2023/1/10 14:21
 */
@Slf4j
@Component
public class CapturePictureHelper {


    public String getFileImage(String indexNo, ChannelInfo channel) {
        String imgFolder = NvrConfigConstant.captureFolder;
        return getFileImage(imgFolder, indexNo, channel);
    }

    /**
     * 抓拍图片
     *
     * @param imgFolder   图片路径
     * @param channelInfo 通道
     */
    private String getFileImage(String imgFolder, String indexNo, ChannelInfo channelInfo) {
//        log.info("-----------这里处理已经getNVRPic----------" + imgPath);
        File file = new File(imgFolder);
        if (!file.exists()) {
            file.mkdir();
        }

        if (channelInfo == null) {
            log.error("通道数据为空");
            return null;
        }
        HCNetSDK.NET_DVR_WORKSTATE deviceWork = new HCNetSDK.NET_DVR_WORKSTATE();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState(lUserID, deviceWork)) {
            // 返回Boolean值，判断是否获取设备能力
            log.error("hkSdk(抓图)-返回设备状态失败" + hCNetSDK.NET_DVR_GetLastError());
        }
        int channelId = channelInfo.getNumber();
        final String path = imgFolder + channelInfo.getName() + "-" + indexNo + ".jpg".trim();
        //非内存直接保存
        //图片质量
        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        //设置图片分辨率
        jpeg.wPicSize = NvrConfigConstant.capturePicSize;
        //设置图片质量
        jpeg.wPicQuality = NvrConfigConstant.capturePicQuality;
        //需要加入通道
//            log.info("-----------这里开始封装 NET_DVR_CaptureJPEGPicture---------");
        boolean is = false;
        try {
            is = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, channelId, jpeg, path.getBytes(SDKConstant.charsetName));

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (is) {
            log.info("hkSdk抓图成功--" + channelInfo.getName() + "-" + indexNo + ".jpg".trim());
            file = new File(path);
            if (!file.exists()) {
                log.warn("抓图文件名异常:" + path);
                File[] files = file.getParentFile().listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        String filename = dir.getPath() + File.separator + name;
                        return filename.contains(path);
                    }
                });

                for (File file1 : files) {
                    log.warn("抓图异常文件名:" + path);
                    file1.renameTo(new File(path));
                }
            }
            return path;
        } else {
            log.warn("文件抓图失败,错误码:" + hCNetSDK.NET_DVR_GetLastError() + ",图片路径" + path);
            return null;
        }

    }

    public StreamFile getMemoryImage(String fileName, ChannelInfo channel) {
        byte[] resBytes = getMemoryImageByte(channel);
        if (resBytes == null) {
            return null;
        }
        StreamFile streamFile = new StreamFile();
        streamFile.setDataByte(resBytes);
        streamFile.setFileName(channel.getName() + "-" + fileName + ".jpg".trim());
        streamFile.setChannelName(channel.getName());
        streamFile.setChannelNumber(channel.getNumber());
        return streamFile;
    }

    //抓图保存到缓冲区(linux)
    private byte[] getMemoryImageByte(ChannelInfo channel) {
        HCNetSDK.NET_DVR_JPEGPARA jpegpara = new HCNetSDK.NET_DVR_JPEGPARA();
        jpegpara.read();
        jpegpara.wPicSize = NvrConfigConstant.capturePicSize;
        jpegpara.wPicQuality = NvrConfigConstant.capturePicQuality;
        jpegpara.write();
        HCNetSDK.BYTE_ARRAY byte_array = new HCNetSDK.BYTE_ARRAY(10 * 1024 * 1024);
        IntByReference ret = new IntByReference(0);
        boolean b = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(lUserID, channel.getNumber(), jpegpara, byte_array.getPointer(), byte_array.size(), ret);
        if (b == false) {
            log.warn("内存抓图失败,错误码:" + hCNetSDK.NET_DVR_GetLastError() + ",通道:" + channel.getName());
            return null;
        }
        byte_array.read();
        byte[] resBytes = byte_array.getPointer().getByteArray(0, ret.getValue());
        return resBytes;
    }

    //抓图保存到缓冲区(linux)
    private ByteArrayDataSource getMemoryImage(ChannelInfo channel) {
        byte[] resBytes = getMemoryImageByte(channel);
        return new ByteArrayDataSource(resBytes, "image/jpeg");
    }



}
