package com.jkddg.nvrmailclient;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

/**
 * @Author 黄永好
 * @create 2023/2/6 16:24
 */
@Slf4j
@Component
public class OpencvInit {
    public static void init(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat mat = Mat.eye( 4, 4, CvType.CV_8UC1 );
        System.out.println( "mat = " + mat.dump() );
        log.info("加载opencv成功！");
    }

}
