package com.jkddg.nvrmailclient.model;

import lombok.Data;

/**
 * @Author 黄永好
 * @create 2023/1/11 15:33
 */
@Data
public class ChannelInfo {
    private int number;
    private String ip;
    private String name;
    private boolean onLine;
}
