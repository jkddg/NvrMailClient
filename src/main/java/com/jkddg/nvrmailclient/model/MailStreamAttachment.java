package com.jkddg.nvrmailclient.model;

import lombok.Data;

import javax.mail.util.ByteArrayDataSource;

/**
 * @Author 黄永好
 * @create 2023/1/12 12:14
 */
@Data
public class MailStreamAttachment {

    private String name;
    private ByteArrayDataSource dataSource;

}