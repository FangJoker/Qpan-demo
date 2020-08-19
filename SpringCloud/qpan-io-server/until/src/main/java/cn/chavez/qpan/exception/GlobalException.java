package cn.chavez.qpan.exception;

import cn.chavez.qpan.support.ResponseEntitySupport;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * @Author: ChavezQiu
 * @description: 全局异常捕获
 * @Date: 2020/2/17 21:40
 */
@Log4j2
@ControllerAdvice
public class GlobalException {

    /**
     * 全局异常
     *
     * @param throwable
     * @return
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<Object> handleException(Throwable throwable) {
        log.error("异常报告", throwable);
        throwable.printStackTrace();
        if (throwable instanceof MissingServletRequestPartException){
            return ResponseEntitySupport.error(HttpStatus.BAD_REQUEST, "参数错误", null);
        }
        return ResponseEntitySupport.error(HttpStatus.INTERNAL_SERVER_ERROR, "网络繁忙，请稍后再试", "网络繁忙");
    }
}
