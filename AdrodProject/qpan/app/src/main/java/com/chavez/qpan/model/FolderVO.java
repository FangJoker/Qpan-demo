package com.chavez.qpan.model;


/**
 * @Author Chavez Qiu
 * @Date 19-12-30.
 * Email：qiuhao1@meizu.com
 * Description：文件夹VO
 */
public class FolderVO extends BaseFileVO {
    /**
     * 可能是个普通文件list，也可能是个目录list
     */
    private Object data;

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
