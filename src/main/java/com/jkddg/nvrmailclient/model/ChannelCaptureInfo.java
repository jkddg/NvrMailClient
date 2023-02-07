package com.jkddg.nvrmailclient.model;

import com.jkddg.nvrmailclient.notify.NotifyBase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author 黄永好
 * @create 2023/1/13 9:17
 */
@Getter
@Setter
public class ChannelCaptureInfo {

    private ChannelInfo channel;
    private List<String> fileImages;
    private List<StreamFile> streamImages;
    private List<StreamFile> peopleImages;
    private List<NotifyBase> notifyList;

}
