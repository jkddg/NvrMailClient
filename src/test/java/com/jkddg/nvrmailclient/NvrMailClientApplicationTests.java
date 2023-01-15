package com.jkddg.nvrmailclient;

import com.jkddg.nvrmailclient.email.MultipleMailService;
import com.jkddg.nvrmailclient.email.SimpleMailService;
import com.jkddg.nvrmailclient.hkHelper.CapturePictureHelper;
import com.jkddg.nvrmailclient.model.MailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {NvrMailClientApplication.class})
class NvrMailClientApplicationTests {

    @Autowired
    SimpleMailService sendMailService;
    @Autowired
    MultipleMailService multipleMailService;
    @Autowired
    CapturePictureHelper capturePictureHelper;

    @Test
    void sendMail() {
        MailRequest mailRequest = new MailRequest();
        mailRequest.setSendTo("jkddg@126.com");
        mailRequest.setText("<i>测试邮件</i>");
        mailRequest.setSubject("java邮件测试");

        multipleMailService.sendMail(mailRequest);
    }

    @Test
    void listen() {
        SDKInit.init();


//        capturePictureHelper.getNVRPic("d:\\",channelList);
    }
}