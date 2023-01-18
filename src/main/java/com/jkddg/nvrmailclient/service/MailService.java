package com.jkddg.nvrmailclient.service;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.constant.SDKConstant;
import com.jkddg.nvrmailclient.email.MultipleMailService;
import com.jkddg.nvrmailclient.model.AlarmMailInfo;
import com.jkddg.nvrmailclient.model.MailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 黄永好
 * @create 2023/1/17 10:44
 */
@Slf4j
@Component
public class MailService {

//    @Autowired
//    private SimpleMailService mailService;

    @Autowired
    private MultipleMailService multipleMailService;

    public MailService() {
//                        MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
//                        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
//                        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
//                        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
//                        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
//                        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
//                        CommandMap.setDefaultCommandMap(mc);
    }

    public void checkAndSendMail() {
        if (SDKConstant.lUserID > -1) {
            log.info("检查发送邮件" + Thread.currentThread().getName() + "," + LocalDateTime.now());
            if (!AlarmService.ALARM_QUEUE.isEmpty()) {
                Map<Integer, AlarmMailInfo> tempMailInfo = new HashMap<>();
                while (!AlarmService.ALARM_QUEUE.isEmpty()) {
                    AlarmMailInfo mailInfo = AlarmService.ALARM_QUEUE.poll();
                    if (mailInfo == null) {
                        break;
                    }
                    if (!tempMailInfo.containsKey(mailInfo.getChannel().getNumber())) {
                        tempMailInfo.put(mailInfo.getChannel().getNumber(), mailInfo);
                    } else {
                        tempMailInfo.get(mailInfo.getChannel().getNumber()).getImages().addAll(mailInfo.getImages());
                    }
                }
                if (!tempMailInfo.isEmpty()) {
                    List<AlarmMailInfo> tempList = new ArrayList<>(tempMailInfo.values());
                    List<String> filePaths = new ArrayList<>();
                    String warnChannel = new String();
                    for (AlarmMailInfo alarmInfo : tempList) {
                        if (StringUtils.hasText(warnChannel)) {
                            warnChannel = warnChannel + "-";
                        }
                        warnChannel = warnChannel + alarmInfo.getChannel().getName() + "(" + alarmInfo.getChannel().getNumber() + ")";
                        filePaths.addAll(alarmInfo.getImages());
                    }
                    if (!CollectionUtils.isEmpty(filePaths)) {

                        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                        //3、发送邮件
                        MailRequest mailRequest = new MailRequest();
                        mailRequest.setSubject(SDKConstant.NvrName + "预警" + warnChannel);
                        mailRequest.setSendTo(NvrConfigConstant.mailTo);
                        mailRequest.setText("录像机预警<br>录像机：" + SDKConstant.NvrName + "<br>通道：" + warnChannel + "<br>时间：" + LocalDateTime.now().toString().replace("T", " "));
                        mailRequest.setFilePath(filePaths);
                        try {
                            multipleMailService.sendMail(mailRequest);
                            for (String filePath : filePaths) {
                                File file = new File(filePath);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        } catch (Exception e) {
                            //                  AlarmService.ALARM_QUEUE.add(mailInfo);
                            log.error("发送邮件失败," + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
