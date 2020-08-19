package cn.chavez.qpan.support.verification;

import cn.chavez.qpan.support.redis.RedisSupport;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;

/**
 * @Author: ChavezQiu
 * @description:验证码功能支持类
 * @Date: 2020/2/17 20:52
 */
@Component
public class VerifySupport {
    @Resource
    RedisSupport redisSupport = new  RedisSupport();

    public  String setVerificationCode(String key) {
        String code = getRandomCode();
        //  5分钟有效
        redisSupport.set(key, code, 300);
        return code.toString();
    }

    public static String getRandomPwd() {
        return getRandomCode() + getRandomCode();
    }

    private static String getRandomCode() {
        Random rd = new Random();
        int r = rd.nextInt(10);
        int r2 = rd.nextInt(10);
        int r3 = rd.nextInt(10);
        int r4 = rd.nextInt(10);
        return String.valueOf(r) + String.valueOf(r2) + String.valueOf(r3) + String.valueOf(r4);
    }
}
