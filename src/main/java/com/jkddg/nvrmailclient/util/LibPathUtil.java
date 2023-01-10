package com.jkddg.nvrmailclient.util;

import java.io.UnsupportedEncodingException;

/**
 * @Author 黄永好
 * @create 2023/1/10 14:53
 */
public class LibPathUtil {
    public static String DLL_PATH;

    static {
//        String path = (LibPathUtil.class.getResource("/").getPath()).replaceAll("%20", " ").substring(1).replace("/", "\\");
        String path="D:\\hklib\\";
        try {
            DLL_PATH = java.net.URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
