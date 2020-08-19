package cn.chavez.qpan.model.file.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/9 16:57
 */
@Data
@ApiModel("文件上传")
public class UploadVo {
    @ApiModelProperty(value = "token", name = "token", example = "", dataType = "String")
    private String token;
    @ApiModelProperty(value = "上传路径uuid", name = "pathUuid", example = "", dataType = "String")
    private String pathUuid;
    @ApiModelProperty(value = "第几片", name = "chunks", example = "", dataType = "String")
    private String chunk;
    @ApiModelProperty(value = "一共有几片", name = "chunks", example = "", dataType = "String")
    private String chunks;
}
