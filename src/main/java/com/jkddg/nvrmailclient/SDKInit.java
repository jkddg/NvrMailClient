package com.jkddg.nvrmailclient;

import com.jkddg.nvrmailclient.constant.NvrConfigConstant;
import com.jkddg.nvrmailclient.util.osSelect;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;

import static com.jkddg.nvrmailclient.constant.SDKConstant.hCNetSDK;

/**
 * @Author 黄永好
 * @create 2023/1/10 15:27
 */
@Slf4j
public class SDKInit {

    public static FExceptionCallBack_Imp fExceptionCallBack;

    static class FExceptionCallBack_Imp implements HCNetSDK.FExceptionCallBack {
        public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {
            log.info("异常事件类型:" + dwType);
        }
    }

    public static void init() {
        log.info("application.yml变量:");
        log.info("serverIp=" + NvrConfigConstant.serverIp);
        log.info("serverPort=" + NvrConfigConstant.serverPort);
        log.info("serverUser=" + NvrConfigConstant.serverUser);
        log.info("serverPwd=" + NvrConfigConstant.serverPwd);
        log.info("capturePicSize=" + NvrConfigConstant.capturePicSize);
        log.info("captureCount=" + NvrConfigConstant.captureCount);
        log.info("captureIntervalSecond=" + NvrConfigConstant.captureIntervalSecond);
        log.info("captureFolder=" + NvrConfigConstant.captureFolder);
        log.info("captureFolderLinux=" + NvrConfigConstant.captureFolderLinux);
        log.info("captureFolderWin=" + NvrConfigConstant.captureFolderWin);
        log.info("capturePicQuality=" + NvrConfigConstant.capturePicQuality);
        log.info("alarmIntervalSecond=" + NvrConfigConstant.alarmIntervalSecond);
        log.info("mailTo=" + NvrConfigConstant.mailTo);
        log.info("mailFrom=" + NvrConfigConstant.mailFrom);
        log.info("mailSSl=" + NvrConfigConstant.mailSSl);
        log.info("channelFlashMinute=" + NvrConfigConstant.channelFlashMinute);
        log.info("linuxLibPath=" + NvrConfigConstant.linuxLibPath);
        log.info("winLibPath=" + NvrConfigConstant.winLibPath);
        if (hCNetSDK == null) {
            if (!createSDKInstance()) {
                log.error("Load SDK fail");
                return;
            }
        }

        //linux系统建议调用以下接口加载组件库
        if (osSelect.isLinux()) {
            HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
            HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
            //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
            String strPath1 = NvrConfigConstant.linuxLibPath + "libcrypto.so.1.1";
            String strPath2 = NvrConfigConstant.linuxLibPath + "libssl.so.1.1";

            System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
            ptrByteArray1.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

            System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
            ptrByteArray2.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

            String strPathCom = NvrConfigConstant.linuxLibPath;
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
        }

        //SDK初始化，一个程序只需要调用一次
        if (!hCNetSDK.NET_DVR_Init()) {
            log.warn("海康sdk初始化失败!");
            return;
        }

        //异常消息回调
        if (fExceptionCallBack == null) {
            fExceptionCallBack = new FExceptionCallBack_Imp();
        }
        Pointer pUser = null;
        if (!hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, fExceptionCallBack, pUser)) {
            return;
        }
        log.info("设置异常消息回调成功");

        //启动SDK写日志
        hCNetSDK.NET_DVR_SetLogToFile(3, "./sdkLog", false);

//        SDKHelper.login_V40("192.168.50.51", (short) 8000, "admin", "abcd123456");

    }

    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean createSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    if (osSelect.isWindows()) {
                        //win系统加载库路径
                        //HCNetSDK INSTANCE = (HCNetSDK) Native.loadLibrary(PropertyUtil.getPath() + "HCNetSDK.dll", HCNetSDK.class);
                        //https://www.cnblogs.com/luxh/p/16599295.html
//                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll";
//                        mvn install:install-file "-DgroupId=net.java.jna" "-DartifactId=jna" "-Dversion=1.0.0" "-Dpackaging=jar" "-Dfile=D:\Mtime\jna.jar"
//                        mvn install:install-file "-DgroupId=net.java.jna" "-DartifactId=examples" "-Dversion=1.0.0" "-Dpackaging=jar" "-Dfile=D:\Mtime\examples.jar"

                        NvrConfigConstant.captureFolder = NvrConfigConstant.captureFolderWin;
                        strDllPath = NvrConfigConstant.winLibPath + "HCNetSDK.dll";
                    } else if (osSelect.isLinux()) {
                        //Linux系统加载库路径
                        NvrConfigConstant.captureFolder = NvrConfigConstant.captureFolderLinux;
                        strDllPath = NvrConfigConstant.linuxLibPath + "libhcnetsdk.so";
                    }
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);

                } catch (Exception ex) {
                    log.info("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }


}
