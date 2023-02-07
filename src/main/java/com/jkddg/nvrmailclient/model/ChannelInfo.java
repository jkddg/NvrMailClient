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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ChannelInfo info = (ChannelInfo) obj;
        if (this.getNumber() == info.getNumber() && this.getName().equals(info.getName()) && this.getIp().equals(info.getIp())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (number + name + ip).hashCode();
    }
}
