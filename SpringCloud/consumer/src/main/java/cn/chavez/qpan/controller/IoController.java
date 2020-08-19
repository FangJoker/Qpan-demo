package cn.chavez.qpan.controller;

import cn.chavez.qpan.model.user.vo.user.MkdirVo;
import cn.chavez.qpan.services.IoService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.chavez.qpan.core.URL.CLIENT_ROOT_URI_IO;
/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/24 2:34
 */
@RequestMapping(CLIENT_ROOT_URI_IO)
@RestController
public class IoController implements  IoService {

    @Autowired
    IoService fileIOService;

    @Override
    @ApiOperation(value = "下载文件", response = ResponseEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uuid", value = "文件uuid", dataType = "String", paramType = "path", required = true),
            @ApiImplicitParam(name = "token", value = "accessToken", dataType = "String", paramType = "query", required = true)
    })
    @GetMapping(value = "/download/{uuid}")
    public void getFileStream(@NonNull @PathVariable String uuid, @NonNull String token) {
        fileIOService.getFileStream(uuid, token);
    }

    @Override
    @ApiOperation(value = "下载文件", response = ResponseEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "uuid", value = "分享记录uuid", dataType = "String", paramType = "path", required = true)
    })
    @GetMapping(value = "/download/share/{uuid}")
    public void getShareFileStream(@NonNull @PathVariable String uuid, @NonNull String password) {
        fileIOService.getShareFileStream(uuid, password);
    }

    @Override
    @ApiOperation(value = "上传文件", response = ResponseEntity.class)
    @PostMapping(value = "/upload", headers = "content-type=multipart/form-data")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "accessToken", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "pathUuid", value = "指定目录uuid", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "chunk", value = "第几片", dataType = "Integer", paramType = "query", required = false),
            @ApiImplicitParam(name = "chunks", value = "总共有几片", dataType = "Integer", paramType = "query", required = false)
    })
    public ResponseEntity<Object> upload(@NonNull String token,
                                         String pathUuid,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam(defaultValue = "1") String chunk,
                                         @RequestParam(defaultValue = "1") String chunks
                                  ) {
        return fileIOService.upload(token, pathUuid, file, chunk, chunks);
    }

    @Override
    @ApiOperation(value = "删除文件", response = ResponseEntity.class)
    @ApiImplicitParam(name = "uuid", value = "文件uuid", dataType = "String", paramType = "path", required = true)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Object> deleteFile(@PathVariable String uuid) {
        return fileIOService.deleteFile(uuid);
    }

    @Override
    @ApiOperation(value = "新建文件夹", response = ResponseEntity.class)
    @PostMapping("/mkdir")
    public ResponseEntity<Object> mkdir(@RequestBody MkdirVo body) {
        return fileIOService.mkdir(body);
    }


}
