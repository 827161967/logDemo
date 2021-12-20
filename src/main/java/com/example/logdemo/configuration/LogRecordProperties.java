package com.example.logdemo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yufeng
 */
@Data
@ConfigurationProperties(prefix = "log-record")
public class LogRecordProperties {

    private RabbitMqProperties rabbitMqProperties;

    private RocketMqProperties rocketMqProperties;

    /**
     * 选择消息类型: rabbitMq, rocketMq
     */
    private String dataPipeline;

    @Data
    public static class RabbitMqProperties {
        private String host;
        private int port;
        private String username;
        private String password;
        private String queueName;
        private String exchangeName;
        private String routingKey;
    }

    @Data
    public static class RocketMqProperties {
        private String topic = "logRecord";
        private String tag = "";
        private String namesrvAddr = "localhost:9876";
        private String groupName = "logRecord";
        private int maxMessageSize = 4000000;
        private int sendMsgTimeout = 3000;
        private int retryTimesWhenSendFailed = 2;
    }
}
