package com.jkddg.nvrmailclient.model;

import lombok.Data;

import javax.mail.util.ByteArrayDataSource;

/**
 * @Author 黄永好
 * @create 2023/1/12 12:14
 */
@Data
public class StreamFile {

    private String channelName;
    private String fileName;
    private ByteArrayDataSource dataSource;
    private byte[] dataByte;
    private boolean identifiedPeople = false;

}