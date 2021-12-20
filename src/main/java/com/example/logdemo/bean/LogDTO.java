package com.example.logdemo.bean;


import lombok.Data;

import java.util.Date;

@Data
public class LogDTO {
    /**
     * 生成的UUID
     */
	private String logId;
    /**
     * 注解中传递的bizId
     */
	private String bizId;
    /**
     * 注解中传递的bizType
     */
	private String bizType;
    /**
     * 若方法执行失败，写入执行的异常信息
     */
	private String exception;
    /**
     * 操作执行的当前时间
     */
	private Date operateDate;
    /**
     * 方式是否执行成功
     */
	private Boolean success;
    /**
     * 注解中传递的msg
     */
	private String msg;
    /**
     * 注解中传递的tag
     */
	private String tag;
    /**
     * 方法执行成功后的返回值（JSON化）
     */
	private String returnStr;

}