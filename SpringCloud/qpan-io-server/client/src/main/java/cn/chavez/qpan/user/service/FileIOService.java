package cn.chavez.qpan.user.service;

import cn.chavez.qpan.model.user.vo.user.MkdirVo;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/5 23:17
 */
public interface FileIOService {
    /**
     * 获取文件流 ，可用于下载（断点重传）也可用于在线打开
     *
     * @param fileUuid
     * @param token
     */
    void getFileStream(String fileUuid, String token);

    /**
     * 获取分享文件
     * @param uuid 记录Uuid
     * @param password
     */
    void getShareFileStream(String uuid, String password);


    /**
     * 文件上传
     *
     * @param token
     * @param pathUuid 上传到自定目录
     * @param file
     * @param chunk    第几片
     * @param chunks   总共有几片
     */
    ResponseEntity<Object> uploadFile(String token, String pathUuid, MultipartFile file, String chunk, String chunks);

    /**
     * 删除文件
     * @param FileUuid
     * @return
     */
    ResponseEntity<Object> deleteFile(String FileUuid);

    /**
     * 新建文件夹
     * @param body
     * @return
     */
    ResponseEntity<Object> mkdir(MkdirVo body);

}
