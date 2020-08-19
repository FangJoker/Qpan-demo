package cn.chavez.qpan.user.aspect;

import cn.chavez.qpan.annotation.IgnoreToken;
import cn.chavez.qpan.support.ResponseEntitySupport;
import cn.chavez.qpan.support.jwt.JWTokenSupport;
import org.apache.http.util.TextUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/3/31 17:04
 */
@Aspect
@Order(1)
@Component
public class AccessTokenAspect {
    @Resource
    private HttpServletRequest request;

    private final String POINT_CUT = "execution(* cn.chavez.qpan.user.controller..*(..))";

    @Around(POINT_CUT)
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (signature.getMethod().isAnnotationPresent(IgnoreToken.class) || signature.getMethod().isAnnotationPresent(IgnoreToken.class)) {
            return joinPoint.proceed();
        } else {
            try {
                return JWTokenSupport.validateJWTokenAspect() ? joinPoint.proceed() : ResponseEntitySupport.error(HttpStatus.UNAUTHORIZED, "无效token", "Invalid token");
            } catch (Exception e) {
                return ResponseEntitySupport.error(HttpStatus.UNAUTHORIZED, e.getMessage(), null);
            }
        }
    }
}
