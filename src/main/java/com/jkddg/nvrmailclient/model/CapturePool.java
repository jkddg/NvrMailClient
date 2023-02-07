package com.jkddg.nvrmailclient.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 黄永好
 * @create 2023/2/7 13:32
 */
public class CapturePool {
    private static Map<ChannelInfo, FixedQueue<StreamFile>> captureMap = new HashMap<>();
    private static Map<ChannelInfo, LinkedBlockingQueue<StreamFile>> capturePeopleMap = new HashMap<>();

    public static void push(ChannelInfo channelInfo, StreamFile streamFile) {
        if (!captureMap.containsKey(channelInfo)) {
            FixedQueue<StreamFile> queue = new FixedQueue<>(1);
            captureMap.put(channelInfo, queue);
        }
        captureMap.get(channelInfo).add(streamFile);
        if (streamFile.isIdentifiedPeople()) {
            if (!capturePeopleMap.containsKey(channelInfo)) {
                LinkedBlockingQueue<StreamFile> queue = new LinkedBlockingQueue<>();
                capturePeopleMap.put(channelInfo, queue);
            }
            capturePeopleMap.get(channelInfo).add(streamFile);
        }
    }

    public static List<StreamFile> poll() {
        List<StreamFile> streamFiles = new ArrayList<>();
        for (ChannelInfo channelInfo : captureMap.keySet()) {
            captureMap.get(channelInfo).drainTo(streamFiles);
        }
        return streamFiles;
    }

    public static List<StreamFile> pollIdentified() {
        List<StreamFile> streamFiles = new ArrayList<>();
        for (ChannelInfo channelInfo : capturePeopleMap.keySet()) {
            capturePeopleMap.get(channelInfo).drainTo(streamFiles);
        }
        return streamFiles;
    }
}
