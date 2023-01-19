package com.jkddg.nvrmailclient.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author 黄永好
 * @create 2023/1/13 9:17
 */
@Getter
@Setter
public class AlarmMailInfo {
    ChannelInfo channel;
    List<String> fileImages;
    List<MailAttachment> streamImages;


}
