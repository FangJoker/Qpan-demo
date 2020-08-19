package cn.chavez.qpan.user.service.impl;

import cn.chavez.qpan.model.user.po.UserPo;
import cn.chavez.qpan.model.user.vo.user.UserLoginVo;
import cn.chavez.qpan.model.user.vo.user.UserRegisterVo;
import cn.chavez.qpan.support.Md5Support;
import cn.chavez.qpan.support.ResponseEntitySupport;
import cn.chavez.qpan.support.jwt.JWTokenSupport;
import cn.chavez.qpan.support.matcher.PhoneMatcherSupport;
import cn.chavez.qpan.support.redis.RedisSupport;
import cn.chavez.qpan.support.verification.VerifySupport;
import cn.chavez.qpan.user.service.LoginService;
import cn.chavez.qpan.orm.mapper.UserMapper;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/3 16:39
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    RedisSupport redisSupport = new RedisSupport();
    @Resource
    VerifySupport verifySupport = new VerifySupport();
    @Resource
    UserMapper userMapper;
    @Autowired
    DataSourceTransactionManager transactionManager;

    private static Logger logger = LogManager.getLogger(LoginServiceImpl.class);

    @Value("${q-pan.md5_salt}")
    private String md5Salt;

    @Value("${q-pan.root_dir}")
    private String rootDir;

    @Override
    public ResponseEntity<Object> login(UserLoginVo body) {
        UserPo user = userMapper.selectByAccount(body.getAccount());
        if (user != null) {
            if (Md5Support.getStringMD5(md5Salt +
                    Md5Support.getStringMD5(body.getPassword())).equals(user.getPassword())) {
                Map<String, Object> jwtMap = new HashMap<>(2);
                jwtMap.put("uuid", user.getUuid());
                JSONObject responseJson = new JSONObject();
                long expirationTime = System.currentTimeMillis()+60 * 1000 * 60;
                responseJson.put("Access-Token", JWTokenSupport.generateJWToken(jwtMap,expirationTime));
                responseJson.put("expiration_time",expirationTime);
                responseJson.put("personal_free_byte",user.getPersonalFreeByte());
                responseJson.put("total_bytes",user.getPersonalTotalByte());
                responseJson.put("account",user.getAccount());
                return ResponseEntitySupport.success(responseJson);
            } else {
                return ResponseEntitySupport.error(HttpStatus.BAD_REQUEST, "account or password is error", null);
            }
        }
        return ResponseEntitySupport.error(HttpStatus.NOT_FOUND, "account not found", null);
    }

    @Override
    public ResponseEntity<Object> checkPhoneIsRegistered(String phoneNumber) {
        return userMapper.selectByAccount(phoneNumber) == null
                ? ResponseEntitySupport.success() :
                ResponseEntitySupport.error(HttpStatus.BAD_REQUEST, "该手机号已经注册", null);
    }

    public static final String TAG = "register_";
    public static final String DEFAULT_PWD = "123456";
    public static final String DEFAULT_NICKNAME = "用户";
    public static final long DEFAULT_PERSONAL_FREE_BYTE = 8 * 1024 * 1024 * 2;
    public static final String ACCESS_KEY = "LTAI4FqmwXqhF7xsCPjuZ9t4";
    public static final String ACCESS_SECRET = "dXF1hF8en4fhhWs59aYPeu0OWBmZeG";

    @Override
    public ResponseEntity<Object> getVerificationCode(String phoneNumber) {
        if (redisSupport.get(TAG + phoneNumber) != null) {
            return ResponseEntitySupport.error(HttpStatus.FORBIDDEN, "请勿重复请求", null);
        }
        if (!PhoneMatcherSupport.isPhoneLegal(phoneNumber)) {
            return ResponseEntitySupport.error(HttpStatus.FORBIDDEN, "手机号无效", null);
        }
        String code = verifySupport.setVerificationCode(TAG + phoneNumber);
        // 短信发送code
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", ACCESS_KEY, ACCESS_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phoneNumber);
        request.putQueryParameter("SignName", "趣盘");
        request.putQueryParameter("TemplateCode", "SMS_183792083");
        JSONObject codeJson = new JSONObject();
        codeJson.put("code", code);
        request.putQueryParameter("TemplateParam", codeJson.toJSONString());
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return ResponseEntitySupport.success();
    }

    @Override
    public ResponseEntity<Object> updateTotalByte(String newBytes) {
       String userUuid = JWTokenSupport.getJWTokenUuid();
       userMapper.updateTotalBytesByUuid(userUuid,Long.parseLong(newBytes));
       return ResponseEntitySupport.success();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> register(UserRegisterVo body) {
        if (!PhoneMatcherSupport.isPhoneLegal(body.getAccount())) {
            return ResponseEntitySupport.error(HttpStatus.FORBIDDEN, "手机号无效", null);
        }
        if (userMapper.selectByAccount(body.getAccount()) != null) {
            return ResponseEntitySupport.error(HttpStatus.BAD_REQUEST, "该手机号已经注册", null);
        }
        String code = (String) redisSupport.get(TAG + body.getAccount());
        if (code == null || !code.equals(body.getVerificationCode())) {
            return ResponseEntitySupport.error(HttpStatus.NOT_FOUND, "验证码错误", null);
        }
        UserPo userPo = new UserPo();
        File userDir = new File(rootDir + "/" + body.getAccount());
        if (!userDir.exists()) {
            if (userDir.mkdir()) {
                logger.info("=====mkdir completed:" + userDir.getAbsolutePath());
                userPo.setDirAddress(userDir.getAbsolutePath());
                String cmdGrant = "chmod -R 777 " + userDir.getAbsolutePath();
                try {
                    Runtime.getRuntime().exec(cmdGrant);
                    logger.info("chmod -r 777=========>" + userDir.getAbsolutePath() + "can read?:" + userDir.canRead() + " can write?:" + userDir.canWrite());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        userPo.setAccount(body.getAccount());
        userPo.setPhoneNumber(body.getAccount());
        userPo.setPassword(Md5Support.getStringMD5(md5Salt + (Md5Support.getStringMD5(DEFAULT_PWD))));
        userPo.setNickName(DEFAULT_NICKNAME + userPo.getAccount());
        userPo.setPersonalFreeByte(DEFAULT_PERSONAL_FREE_BYTE);
        userPo.setPersonalTotalByte(DEFAULT_PERSONAL_FREE_BYTE);

        if (userMapper.insert(userPo) == 1) {
            return ResponseEntitySupport.success();
        } else {
            return ResponseEntitySupport.error(HttpStatus.CONFLICT, "网络繁忙", null);
        }
    }

}
