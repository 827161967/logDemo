package com.example.logdemo.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("cn.monitor4all.logRecord")
@Import({com.example.logdemo.configuration.RabbitMqSenderConfiguration.class, com.example.logdemo.configuration.RocketMqSenderConfiguration.class})
public class LogRecordAutoConfiguration {

}
