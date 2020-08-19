package cn.chavez.qpan.services;

import cn.chavez.qpan.core.URL;
import cn.chavez.qpan.model.user.vo.user.ShareFileVo;
import cn.chavez.qpan.model.user.vo.user.UserLoginVo;
import cn.chavez.qpan.model.user.vo.user.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/15 22:20
 */
@FeignClient(name = "user-service")
public interface UserService {

    @PostMapping(URL.CLIENT_ROOT_URI_USER + "/login")
    ResponseEntity<Object> login(@RequestBody UserLoginVo body);

    @PostMapping(URL.CLIENT_ROOT_URI_USER + "/register")
    ResponseEntity<Object> register(@RequestBody UserRegisterVo body);

    @GetMapping(URL.CLIENT_ROOT_URI_USER + "/getVerifyCode")
    String getVerifyCode(@RequestParam("phoneNumber") String phoneNumber);

    @GetMapping("/dir")
    ResponseEntity<Object> showIndividualDir(@RequestParam("pathUuid")String pathUuid);

    @GetMapping("/searchfile")
    ResponseEntity<Object> searchTarget(@NonNull @RequestParam("target")String target);

    @PostMapping("/shareFile")
    ResponseEntity<Object> shareFile(@RequestBody ShareFileVo body);

    @PutMapping("/updateTotalBytes")
    ResponseEntity<Object> updateTotalByte(@RequestParam("newBytes")String newBytes);

    @GetMapping("/searchShareFile")
    ResponseEntity<Object> searchShareFile(@RequestParam("key")String key);

}
