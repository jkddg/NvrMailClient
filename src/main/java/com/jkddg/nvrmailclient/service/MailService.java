package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.email.MultipleMailService;
import com.jkddg.nvrmailclient.model.ChannelCaptureInfo;
import com.jkddg.nvrmailclient.model.MailRequest;
import com.jkddg.nvrmailclient.model.StreamFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author 黄永好
 * @create 2023/1/17 10:44
 */
@Slf4j
@Component
public class MailService {

    private static LocalDateTime lastMailTime = null;
    private static TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
        }
    };
    @Autowired
    private MultipleMailService multipleMailService;


    public void checkAndSendMail() {
        if (lastMailTime == null) {
            lastMailTime = LocalDateTime.now();
        }
        if (lastMailTime.isBefore(LocalDateTime.now().minusSeconds(NvrConfigConstant.mailIntervalSecond))) {
            //立即执行
//            log.info("邮件立即发送");
            sendMail();
        } else {
            // 每次进来都清零
            timerTask.cancel();
            // 然后创建一个新的任务
            timerTask = new TimerTask() {
                public void run() {
//                    log.info("邮件延时发送");
                    sendMail();
                }
            };
            // 执行任务
            new Timer().schedule(timerTask, 2000);
        }
    }

    private void sendMail() {
        lastMailTime = LocalDateTime.now();
        if (SDKConstant.lUserID > -1) {
//            log.info("检查发送邮件" + Thread.currentThread().getName() + "," + LocalDateTime.now());
            if (!CaptureService.CAPTURE_QUEUE.isEmpty()) {
                Map<Integer, ChannelCaptureInfo> tempMailInfo = new HashMap<>();
                while (!CaptureService.CAPTURE_QUEUE.isEmpty()) {
                    ChannelCaptureInfo mailInfo = CaptureService.CAPTURE_QUEUE.poll();
                    if (mailInfo == null) {
                        break;
                    }
                    if (!tempMailInfo.containsKey(mailInfo.getChannel().getNumber())) {
                        tempMailInfo.put(mailInfo.getChannel().getNumber(), mailInfo);
                    } else {
                        tempMailInfo.get(mailInfo.getChannel().getNumber()).getFileImages().addAll(mailInfo.getFileImages());
                        tempMailInfo.get(mailInfo.getChannel().getNumber()).getStreamImages().addAll(mailInfo.getStreamImages());
                    }
                }
                if (!tempMailInfo.isEmpty()) {
                    List<ChannelCaptureInfo> tempList = new ArrayList<>(tempMailInfo.values());
                    List<String> fileAttachments = new ArrayList<>();
                    List<StreamFile> streamFiles = new ArrayList<>();
                    String warnChannel = new String();
                    for (ChannelCaptureInfo channelCaptureInfo : tempList) {
                        if (StringUtils.hasText(warnChannel)) {
                            warnChannel = warnChannel + "-";
                        }
                        warnChannel = warnChannel + channelCaptureInfo.getChannel().getName();
                        if (!CollectionUtils.isEmpty(channelCaptureInfo.getFileImages())) {
                            fileAttachments.addAll(channelCaptureInfo.getFileImages());
                        }
                        if (!CollectionUtils.isEmpty(channelCaptureInfo.getStreamImages())) {
                            streamFiles.addAll(channelCaptureInfo.getStreamImages());
                        }
                    }
                    if (!CollectionUtils.isEmpty(streamFiles) || !CollectionUtils.isEmpty(fileAttachments)) {

                        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                        //3、发送邮件
                        MailRequest mailRequest = new MailRequest();
                        mailRequest.setSubject(SDKConstant.NvrName + "抓图-" + warnChannel);
                        mailRequest.setSendTo(NvrConfigConstant.mailTo);
                        mailRequest.setText("录像机预警<br>录像机：" + SDKConstant.NvrName + "<br>通道：" + warnChannel + "<br>时间：" + LocalDateTime.now().toString().replace("T", " "));
                        mailRequest.setFileAttachments(fileAttachments);
                        mailRequest.setStreamAttachments(streamFiles);
                        multipleMailService.sendMail(mailRequest);
                        if (!CollectionUtils.isEmpty(fileAttachments)) {
                            for (String filePath : fileAttachments) {
                                File file = new File(filePath);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void sendMail(List<StreamFile> streamFiles) {
        if (!CollectionUtils.isEmpty(streamFiles)) {
            List<String> channelNames = streamFiles.stream().map(p -> {
                return p.getChannelName();
            }).distinct().collect(Collectors.toList());
            String warnChannel = String.join("-", channelNames.toArray(new String[0]));
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            //3、发送邮件
            MailRequest mailRequest = new MailRequest();
            mailRequest.setSubject(SDKConstant.NvrName + "有人抓图-" + warnChannel);
            mailRequest.setSendTo(NvrConfigConstant.mailTo);
            mailRequest.setText("录像机预警<br>录像机：" + SDKConstant.NvrName + "<br>通道：" + warnChannel + "<br>时间：" + LocalDateTime.now().toString().replace("T", " "));
            mailRequest.setStreamAttachments(streamFiles);
            multipleMailService.sendMail(mailRequest);
        }
    }
}
