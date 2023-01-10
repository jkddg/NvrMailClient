package com.jkddg.nvrmailclient.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author 黄永好
 * @create 2023/1/10 12:33
 */
@Getter
@Setter
public class MailRequest {
    /**
     * 接收人
     */
    private String sendTo;

    /**
     *  邮件主题
     */
    private String subject;

    /**
     *  邮件内容
     */
    private String text;

    /**
     *  附件路径
     */
    private List<String> filePath;

}
