package com.jkddg.nvrmailclient;

import com.jkddg.nvrmailclient.util.LibPathUtil;
import com.jkddg.nvrmailclient.util.osSelect;
import com.sun.jna.Native;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NvrMailClientApplication {


    public static void main(String[] args) {
        SpringApplication.run(NvrMailClientApplication.class, args);

        InitSDK.init();
    }


}
