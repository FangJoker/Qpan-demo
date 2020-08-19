package cn.chavez.qpan.user.controller;

import cn.chavez.qpan.annotation.IgnoreToken;
import cn.chavez.qpan.model.user.vo.user.MkdirVo;
import cn.chavez.qpan.uri.SystemUri;
import cn.chavez.qpan.user.service.FileIOService;
import com.github.catalpaflat.valid.annotation.ParameterValid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/5 23:05
 */
@RestController
@Api(description = SystemUri.CLIENT_ROOT_URI_NAME + "文件IO模块")
@RequestMapping(SystemUri.CLIENT_ROOT_URI + "/file")
public class FileIOController {
    @Resource
    FileIOService fileIOService;

    @ApiOperation(value = "下载文件", response = ResponseEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uuid", value = "文件uuid", dataType = "String", paramType = "path", required = true),
            @ApiImplicitParam(name = "token", value = "accessToken", dataType = "String", paramType = "query", required = true)
    })
    @GetMapping(value = "/download/{uuid}")
    @IgnoreToken
    public void getFileStream(@NonNull @PathVariable String uuid, @NonNull String token) {
        fileIOService.getFileStream(uuid, token);
    }

    @ApiOperation(value = "下载文件", response = ResponseEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "uuid", value = "分享记录uuid", dataType = "String", paramType = "path", required = true)
    })
    @GetMapping(value = "/download/share/{uuid}")
    @IgnoreToken
    public void getShareFileStream(@NonNull @PathVariable String uuid, @NonNull String password) {
        fileIOService.getShareFileStream(uuid, password);
    }


    @ApiOperation(value = "上传文件", response = ResponseEntity.class)
    @PostMapping(value = "/upload", headers = "content-type=multipart/form-data")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "accessToken", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "pathUuid", value = "指定目录uuid", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "chunk", value = "第几片", dataType = "Integer", paramType = "query", required = false),
            @ApiImplicitParam(name = "chunks", value = "总共有几片", dataType = "Integer", paramType = "query", required = false)
    })
    @IgnoreToken
    public ResponseEntity<Object> upload(@NonNull String token,
                                         String pathUuid,
                                         @NonNull @ParameterValid(type = MultipartFile.class, msg = "文件不能为空", isMin = true) @RequestParam("file") MultipartFile file,
                                         @RequestParam(defaultValue = "1") String chunk,
                                         @RequestParam(defaultValue = "1") String chunks
                                  ) {
        return fileIOService.uploadFile(token, pathUuid, file, chunk, chunks);
    }

    @ApiOperation(value = "删除文件", response = ResponseEntity.class)
    @ApiImplicitParam(name = "uuid", value = "文件uuid", dataType = "String", paramType = "path", required = true)
    @GetMapping("/delete/{uuid}")
    public ResponseEntity<Object> deleteFile(@PathVariable String uuid) {
        return fileIOService.deleteFile(uuid);
    }

    @ApiOperation(value = "新建文件夹", response = ResponseEntity.class)
    @PostMapping("/mkdir")
    public ResponseEntity<Object> mkdir(@RequestBody MkdirVo body) {
        return fileIOService.mkdir(body);
    }

}
