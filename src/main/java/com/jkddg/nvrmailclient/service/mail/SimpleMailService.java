package com.jkddg.nvrmailclient.service.mail;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.model.MailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;


/**
 * @Author 黄永好
 * @create 2023/1/13 8:40
 */
@Slf4j
@Component
public class SimpleMailService {
    //注入邮件工具类
    @Autowired
    private JavaMailSender javaMailSender;


    public void checkMail(MailRequest mailRequest) {
        Assert.notNull(mailRequest, "邮件请求不能为空");
        Assert.notNull(mailRequest.getSendTo(), "邮件收件人不能为空");
        Assert.notNull(mailRequest.getSubject(), "邮件主题不能为空");
        Assert.notNull(mailRequest.getText(), "邮件收件人不能为空");
    }


    public void sendSimpleMail(MailRequest mailRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        checkMail(mailRequest);
        //邮件发件人
        message.setFrom(NvrConfigConstant.mailFrom);
        //邮件收件人 1或多个
        message.setTo(mailRequest.getSendTo().split(","));
        //邮件主题
        message.setSubject(mailRequest.getSubject());
        //邮件内容
        message.setText(mailRequest.getText());
        //邮件发送时间
        message.setSentDate(new Date());

        javaMailSender.send(message);
        log.info("发送邮件成功:{}->{}", NvrConfigConstant.mailFrom, mailRequest.getSendTo());
    }


    public void sendMail(MailRequest mailRequest) {
        MimeMessage message = javaMailSender.createMimeMessage();
        checkMail(mailRequest);
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            //邮件发件人
            helper.setFrom(NvrConfigConstant.mailFrom);
            //邮件收件人 1或多个
            helper.setTo(mailRequest.getSendTo().split(","));
            //邮件主题
            helper.setSubject(mailRequest.getSubject());
            //邮件内容
            helper.setText(mailRequest.getText(), true);
            //邮件发送时间
            helper.setSentDate(new Date());

            if (!CollectionUtils.isEmpty(mailRequest.getFileAttachments())) {
                for (String filePath : mailRequest.getFileAttachments()) {
                    if (StringUtils.hasText(filePath)) {
                        File attachFile = new File(filePath);
                        if (attachFile.exists()) {
                            FileSystemResource file = new FileSystemResource(attachFile);
                            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
                            helper.addAttachment(fileName, file);
                        }
                    }
                }
            }
            javaMailSender.send(message);
            log.info("发送邮件成功:{}->{}", NvrConfigConstant.mailFrom, mailRequest.getSendTo());
        } catch (MessagingException e) {
            log.error("发送邮件时发生异常！", e);
            throw new RuntimeException(e);
        }
    }

}