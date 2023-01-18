package com.jkddg.nvrmailclient.hkHelper;

import com.jkddg.nvrmailclient.HCNetSDK;
import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteBuffer;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;
import static com.jkddg.nvrmailclient.constant.SDKConstant.lUserID;

/**
 * @Author 黄永好
 * @create 2023/1/10 14:21
 */
@Slf4j
public class CapturePictureHelper {


    public String getNVRPicByConfigPath(String indexNo, ChannelInfo channel) {
        String imgFolder = NvrConfigConstant.captureFolder;
        return getNVRPic(imgFolder, indexNo, channel);
//        return picCutCate(imgFolder, indexNo, channel);
    }

    /**
     * 抓拍图片
     *
     * @param imgFolder   图片路径
     * @param channelInfo 通道
     */
    private String getNVRPic(String imgFolder, String indexNo, ChannelInfo channelInfo) {
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
            is = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, channelId, jpeg, path.getBytes("GBK"));

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

                    ;
                });

                for (File file1 : files) {
                    log.warn("抓图异常文件名:" + path);
                    file1.renameTo(new File(path));
                }
            }
            return path;
        } else {
            log.info("hkSdk(抓图)-抓取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError() + ",图片路径" + path);
            return null;
        }

//        //退出登录
//        hCNetSDK.NET_DVR_Logout(lUserID);
//        //释放SDK资源
//        hCNetSDK.NET_DVR_Cleanup();
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

    public static void main(String[] args) {
        String path = "D:\\capture\\D1-152633-2.jpeg";
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String filename = dir.getPath() + File.separator + name;
                return filename.contains(path);
            }
        };

        File file = new File(path);
        if (!file.exists()) {
            File[] files = file.getParentFile().listFiles(filter);
            for (File file1 : files) {
                file1.renameTo(new File(path));
            }
        }
    }

}
