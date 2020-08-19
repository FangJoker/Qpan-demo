package cn.chavez.qpan.user.service;

import cn.chavez.qpan.model.user.vo.user.UserLoginVo;
import cn.chavez.qpan.model.user.vo.user.UserRegisterVo;
import org.springframework.http.ResponseEntity;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/3 16:20
 */
public interface LoginService {
    /**
     * 登录
     *
     * @param body
     */
    ResponseEntity<Object> login(UserLoginVo body);

    /**
     * 查看手机号码是否被注册
     * @param phoneNumber
     * @return
     */
    ResponseEntity<Object> checkPhoneIsRegistered(String phoneNumber);

    /**
     * 注册
     *
     * @param body
     * @return
     */
    ResponseEntity<Object> register(UserRegisterVo body);

    /**
     * 获取验证码
     * @param phoneNumber
     * @return
     */
    ResponseEntity<Object> getVerificationCode(String phoneNumber);

    /**
     * 更新容量
     * @param newBytes
     * @return
     */
    ResponseEntity<Object> updateTotalByte(String newBytes);
}
