package cn.chavez.qpan.model.user.vo.user;

import cn.chavez.qpan.model.user.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/3 16:22
 */
@Data
@ApiModel("登录VO")
public class UserLoginVo extends BaseVo {
    @ApiModelProperty(value = "账户", name = "account", example = "", dataType = "String")
    private String account;
    @ApiModelProperty(value = "密码", name = "password", example = "", dataType = "String")
    private String password;
}
