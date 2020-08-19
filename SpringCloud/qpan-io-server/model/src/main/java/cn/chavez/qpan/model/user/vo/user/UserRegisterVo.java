package cn.chavez.qpan.model.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/7 18:06
 */
@Data
@ApiModel("注册")
public class UserRegisterVo {
    public UserRegisterVo(){

    }
    @ApiModelProperty(value = "手机号", name = "account", example = "", dataType = "String")
    @NonNull
    private String account;

    @ApiModelProperty(value = "验证码", name = "verificationCode", example = "", dataType = "String")
    @NonNull
    private String verificationCode;
}
