package com.jkddg.nvrmailclient.email;

import com.jkddg.nvrmailclient.model.MailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.*;

/**
 * @Author 黄永好
 * @create 2023/1/13 10:03
 */
@Slf4j
@Component
public class MultipleMailService {
    @Autowired
    private EmailConfig emailConfig;

    private final List<JavaMailSenderImpl> senderList = new ArrayList<>();

    /**
     * 获取MailSender
     *
     * @return CustomMailSender
     */
    public JavaMailSenderImpl getJavaMailSender() {
        if (CollectionUtils.isEmpty(senderList)) {
            Map<String, EmailConfig.MailProperties> mailConfigs = emailConfig.getConfigs();
            log.info("初始化mailSender,mailConfigs={}", mailConfigs);
            for (EmailConfig.MailProperties mailProperties : mailConfigs.values()) {
                log.info("mailConfigs.values(),mailProperties={}", mailProperties);
                // 邮件发送者
                JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
                javaMailSender.setHost(mailProperties.getHost());
                //5、SMTP服务器: 默认端口 换成腾讯云服务器后,需要将端口换成465
                javaMailSender.setPort(mailProperties.getPort());
                //6、//发送邮件协议名称
                javaMailSender.setProtocol("smtp");
                //7、编码格式
                javaMailSender.setDefaultEncoding("UTF-8");
                javaMailSender.setUsername(mailProperties.getUsername());
                javaMailSender.setPassword(mailProperties.getPassword());
                //8、创建连接对象，连接到邮箱服务器
                Properties properties = new Properties();
                //发送服务器需要身份验证,要采用指定用户名密码的方式去认证
                properties.put("mail.smtp.auth", true);
                properties.put("mail.smtp.starttls.enable", true);
                //9、添加连接对象到邮件对象中
                javaMailSender.setJavaMailProperties(properties);
                // 添加数据
                senderList.add(javaMailSender);
            }
        }
        if (senderList.isEmpty()) {
            throw new RuntimeException("轮询javaMailSender为空");
        }
        // 随机返回一个JavaMailSender
        return senderList.get(new Random().nextInt(senderList.size()));
    }

    /**
     * 发送邮件的方法
     *
     * @Param to :给谁发邮件
     * @Param code : 邮件的激活码
     * @Param subject ： 主题
     * @Param text  : 内容
     */
    public void sendMail(MailRequest mailRequest) {
        String userName = "";
        try {
            JavaMailSenderImpl javaMailSender = getJavaMailSender();
            userName = javaMailSender.getUsername();
            MimeMessage mimeMessage = getMimeMessage(javaMailSender.getUsername(), mailRequest.getSendTo(), mailRequest.getSubject(), mailRequest.getText(), mailRequest.getFilePath(), javaMailSender);
            //11、发送邮件
            javaMailSender.send(mimeMessage);
            log.info(javaMailSender.getUsername() + "发往 " + mailRequest.getSendTo() + " 邮件发送成功");
        } catch (MessagingException e) {
            log.error(userName + "发往 " + mailRequest.getSendTo() + " 邮件发送异常", e);
            throw new RuntimeException(e);
        }
    }

    //声明一个Message对象(代表一封邮件),从session中创建
    private MimeMessage getMimeMessage(String username, String toEmail, String subject, String text, List<String> filePaths, JavaMailSenderImpl javaMailSender) throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        //发件人
        mimeMessageHelper.setFrom(username);
        //收件人  : 可以发送给多个收件人，该方法有一个重载的 数组形参
        mimeMessageHelper.setTo(toEmail.split(","));
        //邮件主题
        mimeMessageHelper.setSubject(subject);
        //邮件内容
        mimeMessageHelper.setText(text, true);
        if (!CollectionUtils.isEmpty(filePaths)) {
            for (String filePath : filePaths) {
                if (StringUtils.hasText(filePath)) {
                    File attachFile = new File(filePath);
                    if (attachFile.exists()) {
                        FileSystemResource file = new FileSystemResource(attachFile);
                        String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
                        mimeMessageHelper.addAttachment(fileName, file);
                    }
                }
            }
        }
        return mimeMessage;
    }

}
