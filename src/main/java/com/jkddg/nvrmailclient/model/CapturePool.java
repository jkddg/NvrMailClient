package com.jkddg.nvrmailclient.model;

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
    private static Map<ChannelInfo, FixedQueue<StreamFile>> captureMap = new HashMap<>();
    private static LinkedBlockingQueue<StreamFile> capturePeopleQueue = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<StreamFile> tempCaptureQueue = new LinkedBlockingQueue<>(5);
    private ExecutorService poolExecutor = new ThreadPoolExecutor(2, 4, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy());


    public void push(ChannelInfo channelInfo, StreamFile streamFile) {
        if (!captureMap.containsKey(channelInfo)) {
            FixedQueue<StreamFile> queue = new FixedQueue<>(2);
            captureMap.put(channelInfo, queue);
        }
        captureMap.get(channelInfo).add(streamFile);
        tempCaptureQueue.offer(streamFile);
        poolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                List<StreamFile> streamFiles = new ArrayList<>();
                tempCaptureQueue.drainTo(streamFiles);
                if (CollectionUtils.isEmpty(streamFiles)) {
                    return;
                }
                boolean findPeople = false;
                for (StreamFile file : streamFiles) {
                    byte[] resByte = HumanBodyRecognition.findPeople(file.getDataByte());
                    if (resByte != null) {
                        file.setIdentifiedPeople(true);
                        file.setDataByte(resByte);
                        capturePeopleQueue.add(file);
                        findPeople = true;
//                        file.setDataSource(new ByteArrayDataSource(resByte, "image/jpeg"));
                    }
                }
                if (findPeople) {

                }
            }
        });
    }

    public static List<StreamFile> poll() {
        List<StreamFile> streamFiles = new ArrayList<>();
        for (ChannelInfo channelInfo : captureMap.keySet()) {
            StreamFile streamFile = null;
            while (!captureMap.get(channelInfo).isEmpty()) {
                streamFile = captureMap.get(channelInfo).poll();
            }
            if (streamFile != null) {
                streamFiles.add(streamFile);
            }
        }
        return streamFiles;
    }

    public static List<StreamFile> pollIdentified() {
        List<StreamFile> streamFiles = new ArrayList<>();
        capturePeopleQueue.drainTo(streamFiles);
        return streamFiles;
    }
}
