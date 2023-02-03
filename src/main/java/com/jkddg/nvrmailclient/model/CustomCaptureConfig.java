package com.jkddg.nvrmailclient.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/**
 * @Author 黄永好
 * @create 2023/2/3 16:50
 */
@Getter
@Setter
public class CustomCaptureConfig {
    private LocalTime startTime;
    private LocalTime endTime;
    private int intervalSecond;
}
