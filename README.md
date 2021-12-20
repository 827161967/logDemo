# log-demo

支持使用注解的方式从方法中获取操作日志，并推送到指定数据源

**只需要简单的加上一个@OperationLog便可以将方法的参数，返回结果甚至是异常堆栈通过消息队列发送出去，统一处理。**

```
@OperationLog(bizType = "bizType", bizId = "#request.orderId")
public Response<BaseResult> function(Request request) {
  // 方法执行逻辑
}
```

### 使用方法

**只需要简单的三步：**

**第一步：** SpringBoot项目中引入依赖

```
<dependency>
    <groupId>com.example</groupId>
    <artifactId>logDemo-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

**第二步：** 在Spring配置文件中添加数据源配置

目前支持RabbitMq和RocketMq
```
#log-record.data-pipeline=rabbitMq
log-record.data-pipeline=rocketMq

# 若使用rabbitMq
log-record.rabbit-mq-properties.host=localhost
log-record.rabbit-mq-properties.port=5672
log-record.rabbit-mq-properties.username=admin
log-record.rabbit-mq-properties.password=xxxxxx
log-record.rabbit-mq-properties.queue-name=logRecord
log-record.rabbit-mq-properties.routing-key=
log-record.rabbit-mq-properties.exchange-name=logRecord

# 若使用rocketMq
log-record.rocket-mq-properties.topic=logRecord
log-record.rocket-mq-properties.tag=
log-record.rocket-mq-properties.group-name=logRecord
log-record.rocket-mq-properties.namesrv-addr=localhost:9876
```

**第三步：** 在你自己的项目中，在需要记录日志的方法上，添加注解。

```
@OperationLog(bizType = "bizType", bizId = "#request.orderId")
public Response<BaseResult> function(Request request) {
	// 方法执行逻辑
}
```
- （必填）bizType：业务类型
- （必填）bizId：唯一业务ID（支持SpEL表达式）
- （非必填）msg：需要传递的其他数据（支持SpEL表达式）
- （非必填）tag：自定义标签

