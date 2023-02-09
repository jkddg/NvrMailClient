package com.jkddg.nvrmailclient.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @Author 黄永好
 * @create 2023/1/13 9:26
 */
@Slf4j
@Component
public class OldMailTask {


    /**
     * 任务上次执行时间
     */
//    private static LocalDateTime lastExecutionTime = null;
//    private static List<CustomCaptureConfig> captureConfigs;
//    @Autowired
//    CaptureService captureService;


//    @Scheduled(fixedRate = 2 * 1000)   //定时器定义，设置执行时间
//    @Async("taskPoolExecutor")
//    public void timerMailSend() {
//        if (lastExecutionTime == null) {
//            lastExecutionTime = LocalDateTime.now();
//            captureConfigs = new ArrayList<>();
//            if (StringUtils.hasText(NvrConfigConstant.customCaptureInterval)) {
//                String[] strings = NvrConfigConstant.customCaptureInterval.split(",");
//                if (strings.length > 0) {
//                    for (String s : strings) {
//                        String[] ss = s.split("-");
//                        if (ss.length == 3) {
//                            CustomCaptureConfig customCaptureConfig = new CustomCaptureConfig();
//                            customCaptureConfig.setStartTime(DateUtil.getTimeFromStr(ss[0]));
//                            customCaptureConfig.setEndTime(DateUtil.getTimeFromStr(ss[1]));
//                            customCaptureConfig.setIntervalSecond(Integer.parseInt(ss[2]));
//                            captureConfigs.add(customCaptureConfig);
//                        }
//                    }
//                }
//            }
//            doMailSend();
//            return;
//        }
//        //如果有自定义时间段的，遍历自定义时间段，按自定义频率发送邮件
//        if (!CollectionUtils.isEmpty(captureConfigs)) {
//            for (CustomCaptureConfig captureConfig : captureConfigs) {
//                if (DateUtil.chkInBetweenNow(captureConfig.getStartTime(), captureConfig.getEndTime())) {
//                    if (LocalDateTime.now().minusSeconds(captureConfig.getIntervalSecond()).isAfter(lastExecutionTime)) {
//                        lastExecutionTime = LocalDateTime.now();
//                        doMailSend();
//                    }
//                    return;
//                }
//            }
//        }
//        //按默认频率发送邮件
//        if (LocalDateTime.now().minusSeconds(NvrConfigConstant.defaultCaptureIntervalSecond).isAfter(lastExecutionTime)) {
//            lastExecutionTime = LocalDateTime.now();
//            doMailSend();
//        }
//    }

//    private void doMailSend() {
////        log.info("MailSend");
//        List<ChannelInfo> list = ChannelHelper.getOnLineIPChannels(SDKConstant.lUserID);
//        if (!CollectionUtils.isEmpty(list)) {
//            List<Integer> channels = list.stream().map(ChannelInfo::getNumber).collect(Collectors.toList());
//            captureService.appendCaptureQueue(channels,false);
//        }
//    }



}
