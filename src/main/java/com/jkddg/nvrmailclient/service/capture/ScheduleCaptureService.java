package com.jkddg.nvrmailclient.service.capture;

import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.hkHelper.CapturePictureHelper;
import com.jkddg.nvrmailclient.hkHelper.ChannelHelper;
import com.jkddg.nvrmailclient.hkHelper.LoginHelper;
import com.jkddg.nvrmailclient.model.CapturePool;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import com.jkddg.nvrmailclient.model.StreamFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author 黄永好
 * @create 2023/2/7 12:36
 */
@Slf4j
@Component
public class ScheduleCaptureService {

    @Autowired
    private CapturePool capturePool;
    public final ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(64), new ThreadPoolExecutor.DiscardPolicy());
    @Autowired
    private CapturePictureHelper capturePictureHelper;

    public void captureToPool(List<Integer> channels) {
        if (!CollectionUtils.isEmpty(channels)) {
            if (SDKConstant.lUserID == -1) {
                LoginHelper.loginByConfig();
            }
            if (SDKConstant.lUserID == -1) {
                log.warn("用户未登录，结束接收抓图信息");
                return;
            }
            for (Integer channel : channels) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        //1、判断通道是否在线
                        ChannelInfo channelInfo = ChannelHelper.getOnlineChannelInfoByNo(channel);
                        if (channelInfo == null) {
                            log.warn("通道不在线，通道号：" + channel);
                            ChannelHelper.flashChannel();
                            return;
                        }
                        log.info("通道[" + channelInfo.getName() + "]触发抓图事件");
                        //2、通道截图
                        String picPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                        //内存抓图
                        StreamFile streamFile = capturePictureHelper.getMemoryImage(picPrefix, channelInfo);
                        if (streamFile != null) {
                            capturePool.schedulePush(channelInfo, streamFile);
                        } else {
                            log.warn(channelInfo.getName() + "内存抓图失败");
                        }
                    }
                });
            }
        }
    }
}
