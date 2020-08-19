package cn.chavez.qpan.model.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/12 17:27
 */
@Data
@ApiModel("分享文件")
public class ShareFileVo {
    @ApiModelProperty(value = "分享的文件uuid", name = "uuids", example = "", dataType = "List")
    private List<String> uuids;
    @ApiModelProperty(value = "密码", name = "passWord", example = "", dataType = "String")
    private String passWord;
}
