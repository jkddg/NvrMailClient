package com.jkddg.nvrmailclient.model;

import lombok.Data;

/**
 * @Author 黄永好
 * @create 2023/1/20 10:04
 */
@Data
public class AlarmLockObject {
    public AlarmLockObject() {

    }

    public AlarmLockObject(Integer channelNo) {
        this.channelNo = channelNo;
    }

    private Integer channelNo;
}
