package com.jkddg.nvrmailclient.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author 黄永好
 * @create 2023/1/13 10:04
 */
@Data
@Component
@ConfigurationProperties(prefix = "sender-email", ignoreUnknownFields = false)
public class EmailConfig {

    private Map<String, MailProperties> configs;

    public Map<String, MailProperties> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, MailProperties> configs) {
        this.configs = configs;
    }

    @Data
    public static class MailProperties {
        private String host;
        private int port;
        private String username;
        private String password;
        private String senderName;

    }
}
