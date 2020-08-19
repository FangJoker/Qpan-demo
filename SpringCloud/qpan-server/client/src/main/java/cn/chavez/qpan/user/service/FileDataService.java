package cn.chavez.qpan.user.service;

import cn.chavez.qpan.model.user.vo.user.ShareFileVo;
import org.springframework.http.ResponseEntity;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/3/21 1:27
 */
public interface FileDataService {
    /**
     * 查询用户根目录（或指定目录）列表
     *
     * @param pathUuid 目录uuid
     * @return
     */
    ResponseEntity<Object> queryFileListByUserUuid(String pathUuid);

    /**
     * 搜索目标文件
     *
     * @param target
     * @return
     */
    ResponseEntity<Object> searchTarget(String target);


    /**
     * 分享文件
     *
     * @param body
     * @return
     */
    ResponseEntity<Object> shareFile(ShareFileVo body);

    /**
     * 搜索分享文件
     *
     * @param key 模糊搜索的Key
     * @return
     */
    ResponseEntity<Object> searchShareFile(String key);

    /**
     * 分享文件列表
     *
     * @return
     */
    ResponseEntity<Object> shareFileList();

    /**
     * 上架下架
     * @param uuid  link uuid
     * @param status
     * @return
     */
    ResponseEntity<Object> updateShareFileListStatus(String uuid,int status);
}
