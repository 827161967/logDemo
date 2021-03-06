package com.example.logdemo.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.logdemo.annotation.OperationLog;
import com.example.logdemo.bean.LogDTO;
import com.example.logdemo.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.NamedThreadLocal;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class SystemLogAspect {

    @Autowired
    private LogService logService;

    private static final ThreadLocal<List<LogDTO>> LOGDTO_THREAD_LOCAL = new NamedThreadLocal<>("ThreadLocal logDTOList");

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Before("@annotation(com.example.logdemo.annotation.OperationLog) || @annotation(com.example.logdemo.annotation.OperationLogs)")
    public void doBefore(JoinPoint joinPoint){
        try {

            List<LogDTO> logDTOList = new ArrayList<>();
            LOGDTO_THREAD_LOCAL.set(logDTOList);

            Object[] arguments = joinPoint.getArgs();
            Method method = getMethod(joinPoint);
            OperationLog[] annotations = method.getAnnotationsByType(OperationLog.class);

            // ??????????????????
            for (OperationLog annotation: annotations) {
                // ?????????logDTO
                LogDTO logDTO = new LogDTO();
                logDTOList.add(logDTO);
                String bizIdSpel = annotation.bizId();
                String msgSpel = annotation.msg();
                String bizId = bizIdSpel;
                String extraMsg = msgSpel;

                try {
                    String[] params = discoverer.getParameterNames(method);
                    EvaluationContext context = new StandardEvaluationContext();
                    if (params != null) {
                        for (int len = 0; len < params.length; len++) {
                            context.setVariable(params[len], arguments[len]);
                        }
                    }

                    // bizId ??????????????????????????????????????????????????????????????????????????????
                    if (StringUtils.isNotBlank(bizIdSpel)) {
                        Expression bizIdExpression = parser.parseExpression(bizIdSpel);
                        bizId = bizIdExpression.getValue(context, String.class);
                    }
                    // extraMsg ???????????????????????????????????????
                    if (StringUtils.isNotBlank(msgSpel)) {
                        Expression msgExpression = parser.parseExpression(msgSpel);
                        Object msgObj = msgExpression.getValue(context, Object.class);
                        extraMsg = JSON.toJSONString(msgObj, SerializerFeature.WriteMapNullValue);
                    }

                } catch (Exception e) {
                    log.error("SystemLogAspect doBefore error", e);
                } finally {
                    logDTO.setLogId(UUID.randomUUID().toString());
                    logDTO.setSuccess(true);
                    logDTO.setBizId(bizId);
                    logDTO.setBizType(annotation.bizType());
                    logDTO.setOperateDate(new Date());
                    logDTO.setMsg(extraMsg);
                    logDTO.setTag(annotation.tag());
                }
            }

        } catch (Exception e) {
            log.error("SystemLogAspect doBefore error", e);
        }
    }

    protected Method getMethod(JoinPoint joinPoint) {
        Method method = null;
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature ms = (MethodSignature) signature;
            Object target = joinPoint.getTarget();
            method = target.getClass().getMethod(ms.getName(), ms.getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.error("SystemLogAspect getMethod error", e);
        }
        return method;
    }

    @Around("@annotation(com.example.logdemo.annotation.OperationLog) || @annotation(com.example.logdemo.annotation.OperationLogs)")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        Object result;
        try {
            result = pjp.proceed();
            // logDTO????????????????????? ???????????????????????????????????????????????????
            List<LogDTO> logDTOList = LOGDTO_THREAD_LOCAL.get();
            String returnStr = JSON.toJSONString(result);
            logDTOList.forEach(logDTO -> logDTO.setReturnStr(returnStr));
        } catch (Throwable throwable) {
            // logDTO??????????????????
            List<LogDTO> logDTOList = LOGDTO_THREAD_LOCAL.get();
            logDTOList.forEach(logDTO -> {
                logDTO.setSuccess(false);
                logDTO.setException(throwable.getMessage());
            });
            throw throwable;
        }
        finally {
            // logDTO?????????????????????
            List<LogDTO> logDTOList = LOGDTO_THREAD_LOCAL.get();
            logDTOList.forEach(logDTO -> {
                try {
                    logService.createLog(logDTO);
                } catch (Throwable throwable) {
                    log.error("logRecord send message failure", throwable);
                }
            });
            LOGDTO_THREAD_LOCAL.remove();
        }
        return result;
    }
}
