package com.jkddg.nvrmailclient;

import com.jkddg.nvrmailclient.model.MailRequest;
import com.jkddg.nvrmailclient.service.SendMailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes={NvrMailClientApplication.class})
class NvrMailClientApplicationTests {

    @Autowired
    SendMailService sendMailService;
    @Test
    void sendMail() {
        MailRequest mailRequest=new MailRequest();
        mailRequest.setSendTo("jkddg@126.com");
        mailRequest.setText("<i>测试邮件</i>");
        mailRequest.setSubject("java邮件测试");

        sendMailService.sendHtmlMail(mailRequest);
    }

}