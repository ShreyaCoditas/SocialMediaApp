package com.example.UserModeratorSystem.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.example.UserModeratorSystem.service..*(..))")
    public void serviceMethods(){}

    @Before("serviceMethods()")
    public void logBeforeMethod(JoinPoint joinPoint){
        log.info("excecuting method:{}",joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "serviceMethods()",returning="result")
    public void logAfterMethod(JoinPoint joinPoint,Object result){
        log.info("completed method:{} | result:{}",joinPoint.getSignature().toShortString(),result);
    }

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        long start=System.currentTimeMillis();
        Object result=proceedingJoinPoint.proceed();
        long duration=System.currentTimeMillis()-start;
        log.info("Method:{} excecuted in {} ms", proceedingJoinPoint.getSignature().toShortString(),duration);
        return result;
    }

}

