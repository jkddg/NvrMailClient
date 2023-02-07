package com.jkddg.nvrmailclient.model;

import com.jkddg.nvrmailclient.notify.NotifyBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @Author 黄永好
 * @create 2023/2/7 11:10
 */
@Getter
@Setter
public class AllCaptureInfo {

    private Map<ChannelInfo, ChannelCaptureInfo> channelCaptureInfoMap;
    private List<NotifyBase> notifyList;

    public void appendNotify(List<NotifyBase> notifies){

    }

    public void doNotifyAll() {
        if (!CollectionUtils.isEmpty(notifyList)) {
            for (NotifyBase notifyBase : notifyList) {
                notifyBase.sendNotify(this);
            }
        }
    }
}
