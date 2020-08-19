package cn.chavez.qpan.services;

import cn.chavez.qpan.model.user.vo.user.MkdirVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import static cn.chavez.qpan.core.URL.CLIENT_ROOT_URI_IO;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/24 2:37
 */
@FeignClient(name = "io-service")
public interface IoService {

    /**
     * 下载文件
     * @param uuid
     * @param token
     */
    @GetMapping(value = CLIENT_ROOT_URI_IO +"/download/{uuid}")
    void getFileStream(@NonNull @PathVariable("uuid") String uuid,
                       @NonNull @RequestParam("token") String token);

    /**
     * 下载分享文件
     * @param uuid
     * @param password
     */
    @GetMapping(value = CLIENT_ROOT_URI_IO +"/download/share/{uuid}")
    void getShareFileStream(@PathVariable("uuid") String uuid,
                            @RequestParam("password") String password);

    /**
     * 上传文件
     * @param token
     * @param pathUuid
     * @param file
     * @param chunk
     * @param chunks
     * @return
     */
    @PostMapping(value = CLIENT_ROOT_URI_IO +"/upload", headers = "content-type=multipart/form-data")
    ResponseEntity<Object> upload(@RequestParam("token") String token,
                                  @RequestParam("pathUuid") String pathUuid,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam("chunk") String chunk,
                                  @RequestParam("chunks") String chunks);

    /**
     * 删除文件
     * @param uuid
     * @return
     */
    @DeleteMapping(CLIENT_ROOT_URI_IO +"/{uuid}")
    ResponseEntity<Object> deleteFile(@PathVariable("uuid") String uuid);


    /**
     * 新建文件夹
     * @param body
     * @return
     */
    @PostMapping(CLIENT_ROOT_URI_IO +"/mkdir")
    ResponseEntity<Object> mkdir(@RequestBody MkdirVo body);
}
