package com.jkddg.nvrmailclient.notify;

import com.jkddg.nvrmailclient.model.AllCaptureInfo;
import com.jkddg.nvrmailclient.model.ChannelInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author 黄永好
 * @create 2023/2/7 10:59
 */
@Getter
@Setter
public class NotifyMailGeneral implements NotifyBase {
    @Override
    public void sendNotify(AllCaptureInfo allCaptureInfo) {
        if(allCaptureInfo==null){
            return;
        }
        if(allCaptureInfo.getChannelCaptureInfoMap().isEmpty()){
            return;
        }
        for (ChannelInfo channelInfo : allCaptureInfo.getChannelCaptureInfoMap().keySet()) {

        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            return true;
        }
        return false;
    }
//    @Override
//    public int hashCode() {
//        return (number + name + ip).hashCode();
//    }
}
