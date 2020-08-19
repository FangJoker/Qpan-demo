package cn.chavez.qpan.model.file.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/9 17:26
 */
@Data
@ApiModel("目录树")
public class FileTreeVo {
    private List<BaseFileVo> fileTreeList;
}
