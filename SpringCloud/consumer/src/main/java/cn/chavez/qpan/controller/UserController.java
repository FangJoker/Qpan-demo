package cn.chavez.qpan.controller;

import cn.chavez.qpan.services.UserService;
import cn.chavez.qpan.model.user.vo.user.ShareFileVo;
import cn.chavez.qpan.model.user.vo.user.UserLoginVo;
import cn.chavez.qpan.model.user.vo.user.UserRegisterVo;
import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import static cn.chavez.qpan.core.URL.CLIENT_ROOT_URI_USER;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/14 13:22
 */
@RestController
@RequestMapping(CLIENT_ROOT_URI_USER)
public class UserController implements UserService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UserService providerService;


    @ApiOperation(value = "登录", response = ResponseEntity.class)
    @PostMapping("/login")
    @Override
    public ResponseEntity<Object> login(@RequestBody UserLoginVo body) {
        return providerService.login(body);
    }


    @ApiOperation(value = "注册", response = ResponseEntity.class)
    @PostMapping("/register")
    @Override
    public ResponseEntity<Object> register(@RequestBody UserRegisterVo body) {
        return providerService.register(body);
    }

    @Override
    @HystrixCommand(fallbackMethod = "getVerifyCodeErrorHandle")
    @GetMapping("/getVerifyCode")
    @ApiImplicitParam(name = "phoneNumber", value = "手机号码", dataType = "String", paramType = "query", required = true)
    public String getVerifyCode(@NonNull String phoneNumber) {
        return providerService.getVerifyCode(phoneNumber);
    }

    @GetMapping("/dir")
    @ApiOperation(value = "展示文件树", response = ResponseEntity.class)
    @ApiImplicitParam(name = "pathUuid", value = "目录uuid", dataType = "String", paramType = "query", required = false)
    @Override
    public ResponseEntity<Object> showIndividualDir(String pathUuid) {
        return providerService.showIndividualDir(pathUuid);
    }


    @ApiOperation(value = "搜索文件", response = ResponseEntity.class)
    @ApiImplicitParam(name = "target", value = "目标文件名", dataType = "String", paramType = "query", required = true)
    @GetMapping("/searchfile")
    @Override
    public ResponseEntity<Object> searchTarget(@NonNull String target) {
        return providerService.searchTarget(target);
    }


    @ApiOperation(value = "分享文件", response = ResponseEntity.class)
    @PostMapping("/shareFile")
    @Override
    public ResponseEntity<Object> shareFile(@RequestBody ShareFileVo body) {
        return providerService.shareFile(body);
    }


    @ApiImplicitParam(name = "newBytes", value = "新容量", dataType = "String", paramType = "query", required = true)
    @PutMapping("/updateTotalBytes")
    @Override
    public ResponseEntity<Object> updateTotalByte(String newBytes) {
        return providerService.updateTotalByte(newBytes);
    }


    @ApiOperation(value = "搜索分享文件", response = ResponseEntity.class)
    @GetMapping("/searchShareFile")
    @Override
    public ResponseEntity<Object> searchShareFile(String key) {
        return providerService.searchShareFile(key);
    }


    public String getVerifyCodeErrorHandle(String phoneNumber) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("msg", "request repeat");
        return errorJson.toJSONString();
    }
}
