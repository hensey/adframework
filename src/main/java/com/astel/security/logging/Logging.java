package com.astel.security.logging;

import lombok.extern.log4j.Log4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j
public class Logging {
    @After(value = "@annotation(com.astel.security.services.SecuredService)")
    public void after(JoinPoint joinPoint) throws Throwable {
        log.info(joinPoint.getTarget().getClass().getName() + " " + joinPoint.getSignature().getName());
    }
}