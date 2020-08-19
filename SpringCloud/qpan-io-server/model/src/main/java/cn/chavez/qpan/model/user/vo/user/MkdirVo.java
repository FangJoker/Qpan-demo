package cn.chavez.qpan.model.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/12 23:25
 */
@Data
@ApiModel("新建文件夾")
public class MkdirVo {
    @ApiModelProperty(value = "當前目錄uuid", name = "uuid", example = "", dataType = "List")
    String uuid;
    @ApiModelProperty(value = "文件夹名", name = "name", example = "", dataType = "String")
    String name;
}
