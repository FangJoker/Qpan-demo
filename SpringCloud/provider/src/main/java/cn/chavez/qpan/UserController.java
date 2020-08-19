package cn.chavez.qpan;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/14 13:10
 */
@RestController
public class UserController {
    @RequestMapping(value = "/provider", method = RequestMethod.POST)
    public String changeUser(@RequestBody User user) {
        System.out.println(user.toString());
        user.setName("chavez");
        return JSONObject.toJSONString(user);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String changeUser() {
        System.out.println("get test");
        return "provider test";
    }
}
