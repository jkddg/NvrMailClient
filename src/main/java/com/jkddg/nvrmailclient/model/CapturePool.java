package com.jkddg.nvrmailclient.model;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.opencv.HumanBodyRecognition;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author 黄永好
 * @create 2023/2/7 13:32
 */
@Component
public class CapturePool {
    private static Map<ChannelInfo, FixedQueue<StreamFile>> scheduleCaptureMap = new HashMap<>();
    private static Map<ChannelInfo, FixedQueue<StreamFile>> alarmCaptureMap = new HashMap<>();
    private static LinkedBlockingQueue<StreamFile> capturePeopleQueue = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<StreamFile> tempCaptureQueue = new LinkedBlockingQueue<>(5);
    private ExecutorService poolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy());



    public void schedulePush(ChannelInfo channelInfo, StreamFile streamFile) {
        if (!scheduleCaptureMap.containsKey(channelInfo)) {
            FixedQueue<StreamFile> queue = new FixedQueue<>(2);
            scheduleCaptureMap.put(channelInfo, queue);
        }
        scheduleCaptureMap.get(channelInfo).add(streamFile);
        tempCaptureQueue.offer(streamFile);
        poolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                List<StreamFile> streamFiles = new ArrayList<>();
                tempCaptureQueue.drainTo(streamFiles);
                if (CollectionUtils.isEmpty(streamFiles)) {
                    return;
                }
                for (StreamFile file : streamFiles) {
                    byte[] resByte = HumanBodyRecognition.findPeople(file.getDataByte());
                    if (resByte != null) {
                        file.setIdentifiedPeople(true);
                        file.setDataByte(resByte);
                        capturePeopleQueue.add(file);
                    }
                }
            }
        });
    }

    public void alarmPush(ChannelInfo channelInfo, StreamFile streamFile) {
        if (!alarmCaptureMap.containsKey(channelInfo)) {
            FixedQueue<StreamFile> queue = new FixedQueue<>(NvrConfigConstant.alarmCaptureCount);
            alarmCaptureMap.put(channelInfo, queue);
        }
        alarmCaptureMap.get(channelInfo).add(streamFile);
    }

    public static List<StreamFile> pollAlarm() {
        List<StreamFile> streamFiles = new ArrayList<>();
        for (ChannelInfo channelInfo : alarmCaptureMap.keySet()) {
            List<StreamFile> queueList = new ArrayList<>();
            alarmCaptureMap.get(channelInfo).drainTo(queueList);
            if (queueList.size() > 0) {
                streamFiles.addAll(queueList);
            }
        }
        return streamFiles;
    }

    public static List<StreamFile> pollSchedule() {
        List<StreamFile> streamFiles = new ArrayList<>();
        for (ChannelInfo channelInfo : scheduleCaptureMap.keySet()) {
            List<StreamFile> queueList = new ArrayList<>();
            scheduleCaptureMap.get(channelInfo).drainTo(queueList);
            if (queueList.size() > 0) {
                streamFiles.add(queueList.get(queueList.size() - 1));
            }
        }
        return streamFiles;
    }

    public static List<StreamFile> pollPeople() {
        List<StreamFile> streamFiles = new ArrayList<>();
        capturePeopleQueue.drainTo(streamFiles);
        return streamFiles;
    }
}
