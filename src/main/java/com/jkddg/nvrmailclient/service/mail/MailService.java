package com.jkddg.nvrmailclient.service.mail;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.model.MailRequest;
import com.jkddg.nvrmailclient.model.StreamFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static com.jkddg.nvrmailclient.service.MailCaptureService.CAPTURE_QUEUE;

/**
 * @Author 黄永好
 * @create 2023/1/17 10:44
 */
@Slf4j
@Component
public class MailService {

    private static LocalDateTime lastMailTime = null;
    private static TimerTask timerTask = null;

    @Autowired
    private MultipleMailService multipleMailService;


    /**
     * 消除抖动的发送邮件
     */
    public void sendAlarmMailNoShake() {
        if (lastMailTime == null) {
            lastMailTime = LocalDateTime.now();
        }
        if (lastMailTime.isBefore(LocalDateTime.now().minusSeconds(NvrConfigConstant.mailIntervalSecond))) {
            //立即执行
//            log.info("邮件立即发送");
            sendAlarmMail();
        } else {
            // 每次进来都清零
            if (timerTask != null) {
                timerTask.cancel();
            }
            // 然后创建一个新的任务
            timerTask = new TimerTask() {
                public void run() {
//                    log.info("邮件延时发送");
                    sendAlarmMail();
                }
            };
            // 执行任务
            new Timer().schedule(timerTask, 2000);
        }
    }

    private void sendAlarmMail() {
        lastMailTime = LocalDateTime.now();
        if (SDKConstant.lUserID > -1) {
//            log.info("发送预警邮件" + Thread.currentThread().getName() + "," + LocalDateTime.now());
            List<StreamFile> streamFiles = new ArrayList<>();
            CAPTURE_QUEUE.drainTo(streamFiles);
            if (!CollectionUtils.isEmpty(streamFiles)) {
                String warnChannel = new String();
                boolean findPeople = false;
                for (StreamFile streamFile : streamFiles) {
                    if (StringUtils.hasText(warnChannel)) {
                        warnChannel = warnChannel + "-";
                    }
                    warnChannel = warnChannel + streamFile.getChannelName();
                    if (streamFile.isIdentifiedPeople()) {
                        findPeople = true;
                    }
                }
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                //3、发送邮件
                MailRequest mailRequest = new MailRequest();
                mailRequest.setSubject(SDKConstant.NvrName + (findPeople ? "[有人]" : "") + "抓图-" + warnChannel);
                mailRequest.setSendTo(NvrConfigConstant.mailTo);
                mailRequest.setText("录像机预警<br>录像机：" + SDKConstant.NvrName + "<br>通道：" + warnChannel + "<br>时间：" + LocalDateTime.now().toString().replace("T", " "));
                mailRequest.setStreamAttachments(streamFiles);
                multipleMailService.sendMail(mailRequest);
            }
        }
    }

    public void sendMail(List<StreamFile> streamFiles, String extInfo) {
        if (!CollectionUtils.isEmpty(streamFiles)) {
            List<String> channelNames = streamFiles.stream().map(p -> {
                return p.getChannelName();
            }).distinct().collect(Collectors.toList());
            String warnChannel = String.join("-", channelNames.toArray(new String[0]));
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            //3、发送邮件
            MailRequest mailRequest = new MailRequest();
            mailRequest.setSubject(SDKConstant.NvrName + extInfo + warnChannel);
            mailRequest.setSendTo(NvrConfigConstant.mailTo);
            mailRequest.setText("录像机预警<br>录像机：" + SDKConstant.NvrName + "<br>通道：" + warnChannel + "<br>时间：" + LocalDateTime.now().toString().replace("T", " "));
            mailRequest.setStreamAttachments(streamFiles);
            multipleMailService.sendMail(mailRequest);
        }
    }


}
