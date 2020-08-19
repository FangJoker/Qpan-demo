package cn.chavez.qpan.user.service.impl;

import cn.chavez.qpan.model.file.po.FilePo;
import cn.chavez.qpan.model.file.vo.UploadVo;
import cn.chavez.qpan.model.user.vo.user.MkdirVo;
import cn.chavez.qpan.model.user.vo.user.ShareFileVo;
import cn.chavez.qpan.orm.mapper.FileMapper;
import cn.chavez.qpan.orm.mapper.ShareRecordMapper;
import cn.chavez.qpan.orm.mapper.UserMapper;
import cn.chavez.qpan.support.Md5Support;
import cn.chavez.qpan.support.ResponseEntitySupport;
import cn.chavez.qpan.support.executor.UploadWorkers;
import cn.chavez.qpan.support.file.BaseFileTo;
import cn.chavez.qpan.support.file.FileManagerSupport;
import cn.chavez.qpan.support.jwt.JWTokenSupport;
import cn.chavez.qpan.user.service.FileIOService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/5 23:28
 */
@Service
public class FileIOServiceImpl implements FileIOService {
    @Resource
    UserMapper userMapper;
    @Resource
    FileMapper fileMapper;
    @Resource
    ShareRecordMapper shareRecordMapper;

    @Value("${uploadFolder}")
    private String baseUpLoadPath;

    private static Logger logger = LogManager.getLogger(FileIOServiceImpl.class);

    @Override
    public void getFileStream(String fileUuid,
                              String token
                        ) {
        ServletRequestAttributes res = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = res.getRequest();
        HttpServletResponse response = res.getResponse();
        getStream(fileUuid, true, token,request,response);
    }

    @Override
    public void getShareFileStream(String uuid, String password) {
        ServletRequestAttributes res = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = res.getRequest();
        HttpServletResponse response = res.getResponse();
        String fileUuid = shareRecordMapper.selectShareFileUuidByShareRecordUuidAndPassWord(uuid, password);
        if (fileUuid == null) {
            System.out.println("密码错误");
            streamResponseHandle(response, HttpStatus.UNAUTHORIZED, "密码错误");
            return;
        } else {
            getStream(fileUuid, false, null, request, response);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<Object> uploadFile(String token,
                                             String pathUuid,
                                             MultipartFile file,
                                             String chunk,
                                             String chunks
                                    ) {
        ServletRequestAttributes res = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = res.getRequest();
        HttpServletResponse response = res.getResponse();

        // 验证token
        String userUuid;
        try {
            userUuid = validUserUuidHandle(token, response);
        } catch (IllegalArgumentException e) {
            return responseHandle(response, HttpStatus.UNAUTHORIZED, "token过期");
        }
        // 判断是否为文件上传
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return responseHandle(response, HttpStatus.FORBIDDEN, "请求参数错误");
        }
        // 校验chunk合法性
        if (Integer.parseInt(chunk) > Integer.parseInt(chunks)) {
            return responseHandle(response, HttpStatus.FORBIDDEN, "分片参数错误");
        }
        String fileName = file.getOriginalFilename();
        String individualRootDir = userMapper.selectIndividualDir(userUuid);
        // 如果已经存在的文件则不需要再次传输
//        List<BaseFileTo> searchFileResultList = new ArrayList<>();
//        FileManagerSupport.searchFileInRoot(individualRootDir, file.getOriginalFilename(), searchFileResultList);
//        if (searchFileResultList.size() > 0) {
//            fileMapper.updateDeleted(fileMapper.selectFileUuidByPath(searchFileResultList.get(0).getPath()), 0);
//            return responseHandle(response, HttpStatus.OK, "上传成功");
//        }
        long currentTime = System.currentTimeMillis();
        // 上传路径默认为用户根目录
        String uploadDestinationDir = individualRootDir;
        try {
            // 生成分片文件名
            System.out.println("分片:" + fileName);
            String tempPartFileName = Md5Support.getStringMD5(fileName.substring(0, fileName.indexOf("."))) + "_" + String.valueOf(currentTime) + "_part_" + chunk;
            System.out.println("分片文件名:" + tempPartFileName);
            String tempDirName;
            System.out.println("path Uuid: " + pathUuid);
            // 验证指定目录是否合法
            if (!TextUtils.isBlank(pathUuid)) {
                String path = fileMapper.selectPathByUuid(pathUuid);
                if (!FileManagerSupport.isBelongToRoot(path, individualRootDir)) {
                    logger.info("用户:" + userUuid + "\t非法访问路径:" + path);
                    return responseHandle(response, HttpStatus.FORBIDDEN, "无权限访问目录:" + pathUuid);
                }
                uploadDestinationDir = path;
                // 生成临时目录，存放分片文件
                tempDirName = path + "/temp/" + String.valueOf(currentTime);
            } else {
                tempDirName = individualRootDir + "/temp/" + String.valueOf(currentTime);
            }
            System.out.println("临时目录:" + tempDirName);
            File tempDir = new File(tempDirName);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            // 分片处理
            File tempPartFile = new File(tempDir, tempPartFileName);
            try {
                FileUtils.copyInputStreamToFile(file.getInputStream(), tempPartFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 如果是最后一块分片完就合并
            if (Integer.parseInt(chunk) == Integer.parseInt(chunks)) {
                mergeFile(tempDirName, uploadDestinationDir, fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("文件" + chunk + "-" + chunks + fileName + "上传失败");
            return responseHandle(response, HttpStatus.CONFLICT, "上传失败");
        }
        FilePo filePo = new FilePo();
        filePo.setFileName(fileName);
        filePo.setFilePath(uploadDestinationDir + "/" + fileName);
        filePo.setFileType(FileManagerSupport.getFileType(fileName));
        filePo.setTotalBytes(file.getSize());
        filePo.setUserUuid(userUuid);
        if (fileMapper.insertFile(filePo) == 1) {
            // 更新个人空间容量
            Long oldFreeBytes = userMapper.selectFreeBytesByUuid(userUuid);
            Long newFreeBytes = oldFreeBytes - filePo.getTotalBytes();
            userMapper.updateFreeBytesByUuid(newFreeBytes,userUuid);
            return responseHandle(response, HttpStatus.OK, "上传成功");
        } else {
            logger.info("文件" + chunk + "-" + chunks + fileName + "信息写入数据库失败");
            return responseHandle(response, HttpStatus.CONFLICT, "网络繁忙，请稍后再试");
        }
    }

    @Override
    public ResponseEntity<Object> deleteFile(String fileUuid) {
        FilePo filePo = fileMapper.selectFilePOByFileUuid(fileUuid);
        if (filePo != null) {
            System.out.println("delete:"+filePo.getFilePath());
            File file = new File(filePo.getFilePath());
            if (file.exists()) {
               if (filePo.getFileType() == FileManagerSupport.IS_FOLDER){
                   List<BaseFileTo> fileTos =  FileManagerSupport.getFileList(file.getAbsolutePath());
                   for (BaseFileTo folderFile: fileTos){
                       String foldFileUuid = fileMapper.selectFileUuidByPath(folderFile.getPath());
                       fileMapper.updateDeleted(foldFileUuid, 1);
                   }
                   fileMapper.updateDeleted(filePo.getUuid(), 1);
                   return ResponseEntitySupport.success();
               }else {
                   fileMapper.updateDeleted(filePo.getUuid(), 1);
                   return ResponseEntitySupport.success();
               }
            } else {
                return ResponseEntitySupport.error(HttpStatus.NOT_FOUND, "文件不存在", null);
            }
        } else {
            return ResponseEntitySupport.error(HttpStatus.CONFLICT, "网络繁忙", null);
        }
    }

    @Override
    public ResponseEntity<Object> mkdir(MkdirVo body) {
        String userUuid = JWTokenSupport.getJWTokenUuid();
        String individualRootDir = userMapper.selectIndividualDir(userUuid);
        String pathUuid = body.getUuid();
        String dirName = body.getName();
        if (pathUuid != null) {
            FilePo currentDir = fileMapper.selectFilePOByFileUuid(pathUuid);
            // 校验目录
            if (validFilePermission(currentDir.getFilePath(), individualRootDir)) {
                File newDir = new File(currentDir.getFilePath(), dirName);
                return mkdir(newDir);
            } else {
                return ResponseEntitySupport.error(HttpStatus.FORBIDDEN, "无权限访问", null);
            }
        } else {
            // 如果没传pathUuid 则在根目录下创建
            File newDir = new File(individualRootDir, dirName);
            return mkdir(newDir);
        }

    }

    private ResponseEntity<Object> mkdir(File newDir) {
        // 判断文件夹是否已存在
        if (!newDir.exists()) {
            if (newDir.mkdir()) {
                FilePo filePo = new FilePo();
                filePo.setFileType(FileManagerSupport.IS_FOLDER);
                filePo.setTotalBytes(0);
                filePo.setFileName(newDir.getName());
                filePo.setFilePath(newDir.getAbsolutePath());
                filePo.setUserUuid(JWTokenSupport.getJWTokenUuid());
                fileMapper.insertFile(filePo);
                return ResponseEntitySupport.success();
            } else {
                return ResponseEntitySupport.error(HttpStatus.CONFLICT, "网络繁忙", null);
            }
        } else {
            return ResponseEntitySupport.error(HttpStatus.FORBIDDEN, "该文件夹已存在", null);
        }
    }

    private ResponseEntity<Object> responseHandle(HttpServletResponse response, HttpStatus httpStatus, String msg) {
        try {
            OutputStream out = null;
            out = response.getOutputStream();
            response.setHeader("Content-Type", "text/html;charset=UTF-8");
            ResponseEntity<Object> responseEntity;
            if (httpStatus == HttpStatus.OK) {
                return ResponseEntitySupport.success(msg, null);
            } else {
                return ResponseEntitySupport.error(httpStatus, msg, null);
            }
//            String responseJsonString = JSONObject.toJSONString(responseEntity);
//            out.write(responseJsonString.getBytes("UTF-8"));
//            out.flush();

        } catch (IOException e) {
            return ResponseEntitySupport.error(HttpStatus.CONFLICT, "网络繁忙", null);
        }
    }

    private void streamResponseHandle(HttpServletResponse response, HttpStatus httpStatus, String msg) {
        try {
            OutputStream out = null;
            out = response.getOutputStream();
            response.setHeader("Content-Type", "text/html;charset=UTF-8");
            ResponseEntity<Object> responseEntity;
            if (httpStatus == HttpStatus.OK) {
                responseEntity = ResponseEntitySupport.success(msg, null);
            } else {
                response.setStatus(httpStatus.value());
                responseEntity = ResponseEntitySupport.error(httpStatus, msg, null);
            }
            String responseJsonString = JSONObject.toJSONString(responseEntity);
            out.write(responseJsonString.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    private FilePo getFilePoByUuid(String uuid) {
        return fileMapper.selectFilePOByFileUuid(uuid);
    }

    private String getUserUuidByValidAccessToken(String token) {
        String userUuid;
        try {
            userUuid = JWTokenSupport.getJWTokenUuid(token);
            return userUuid;
        } catch (IllegalStateException e) {
            logger.info(e.getMessage());
            return null;
        }
    }

    private String validUserUuidHandle(String token, HttpServletResponse response) {
        String userUuid = getUserUuidByValidAccessToken(token);
        if (userUuid == null) {
            throw new IllegalArgumentException("invalid token");
        } else {
            return userUuid;
        }
    }

    private Boolean validFilePermission(String filePath, String individualRootDir) {
        return FileManagerSupport.isBelongToRoot(filePath, individualRootDir);
    }

    /**
     * 传输下载资源
     * 因为使用了 RandomAccessFile 类所以要特殊处理下最后一段的传输
     *
     * @param downloadSize
     * @param in
     * @param out
     */
    private static void transportDownloadFileData(Long downloadSize, RandomAccessFile in, OutputStream out) {
        int bufferLen = (int) (downloadSize < 2048 ? downloadSize : 2048);
        byte[] buffer = new byte[bufferLen];
        int pos = 0;
        // 请求已传输的数据大小
        long count = 0;
        try {
            while ((pos = (in.read(buffer))) != -1) {
                out.write(buffer, 0, pos);
                count += pos;
                // 判断是否到了最后一段,处理最后一段不满缓冲大小的传输，否则会读取错误的位置
                if (downloadSize - count < bufferLen) {
                    bufferLen = (int) (downloadSize - count);
                    if (bufferLen == 0) {
                        break;
                    }
                    buffer = new byte[bufferLen];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("数据传输被暂停或中断");
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.info("数据传输被暂停或中断");
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.info("数据传输被暂停或中断");
                }
            }
        }
    }

    /**
     * 合并文件
     *
     * @param destPath     合并后文件目录
     * @param destFileName 合并后的目标文件名
     * @param mergePath    需要合并的目录
     */
    private void mergeFile(String mergePath, String destPath, String destFileName) {
        System.out.println("开始合并文件");
        File mergeDir = new File(mergePath);
        File destFile = new File(destPath + "/" + destFileName);
        if (mergeDir.exists() && mergeDir.isDirectory()) {
            File[] partFiles = mergeDir.listFiles();
            try {
                FileOutputStream destFileOutputStream = new FileOutputStream(destFile, true);
                for (File partFile : partFiles) {
                    FileUtils.copyFile(partFile, destFileOutputStream);
                    destFileOutputStream.close();
                }
                // 删除分片临时文件夹
                FileUtils.deleteDirectory(mergeDir);
                System.out.println("合并文件完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件流
     *
     * @param fileUuid
     * @param needCheck 是否需要验证文件权限
     * @param token     不需要验证权限不填
     * @param request
     * @param response
     */
    private void getStream(String fileUuid,
                           boolean needCheck,
                           String token,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        FilePo filePO = getFilePoByUuid(fileUuid);
        if (filePO == null) {
            streamResponseHandle(response, HttpStatus.NOT_FOUND, "no resource");
            return;
        }
        String filePath = filePO.getFilePath();
        // 是否验证目录权限
        if (needCheck) {
            System.out.println("需要检验权限");
            String userUuid = "";
            try {
                userUuid = validUserUuidHandle(token, response);
            } catch (IllegalArgumentException e) {
                streamResponseHandle(response, HttpStatus.UNAUTHORIZED, "token过期");
            }
            String individualRootDir = userMapper.selectIndividualDir(userUuid);
            System.out.println("filePath:"+filePath+" individualRootDir:"+individualRootDir);
            System.out.println("result: "+validFilePermission(filePath, individualRootDir));
            if (validFilePermission(filePath, individualRootDir)) {
                System.out.println("开始io");
                IoTransition(filePath, request, response);
            } else {
                streamResponseHandle(response, HttpStatus.FORBIDDEN, "无权限访问");
            }
        } else {
            IoTransition(filePath, request, response);
            return;
        }
    }

    private void IoTransition(String filePath, HttpServletRequest request,
                              HttpServletResponse response) {
        File downloadFile = new File(filePath);
        System.out.println("Begain to download: " + filePath);
        logger.info("Begain to download: " + filePath);
        if (downloadFile.exists()) {
            ServletContext context = request.getServletContext();
            String mimeType = context.getMimeType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
            response.setHeader(headerKey, headerValue);
            // 设置断点续传头部
            response.setHeader("Accept-Ranges", "bytes");
            long downloadFileSize = downloadFile.length();
            long fromPos = 0, toPos = 0;
            if (request.getHeader("Range") == null) {
                // 如果客户端没有在头部设置range 则返回全文件大小
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Content-Length", downloadFileSize + "");
            } else {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                String range = request.getHeader("Range");
                String bytes = range.replaceAll("bytes=", "");
                String[] fromToPosAry = bytes.split("-");
                fromPos = Long.parseLong(fromToPosAry[0]);
                if (fromToPosAry.length == 2) {
                    toPos = Long.parseLong(fromToPosAry[1]);
                }
                // 根据客户端Range值计算应该返回多少字节
                int size;
                if (toPos > fromPos) {
                    size = (int) (toPos - fromPos);
                } else {
                    size = (int) (downloadFileSize - fromPos);
                }
                response.setHeader("Content-Length", size + "");
                if (toPos <= 0) {
                    toPos = size;
                }
                response.setHeader("Content-Range", "bytes " + fromPos + "-" + toPos + "/" + downloadFileSize);
                System.out.println("Content-Range " + "bytes " + fromPos + "-" + toPos + "/" + downloadFileSize);
                logger.info("Content-Range " + "bytes " + fromPos + "-" + toPos + "/" + downloadFileSize);
                downloadFileSize = size;
            }
            // Copy the stream to the response's output stream.
            RandomAccessFile in = null;
            OutputStream out = null;
            try {
                in = new RandomAccessFile(downloadFile, "rw");
                out = response.getOutputStream();
                // 设置下载起始位置
                if (fromPos > 0) {
                    in.seek(fromPos);
                }
                transportDownloadFileData(downloadFileSize, in, out);
                response.flushBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            streamResponseHandle(response, HttpStatus.NOT_FOUND, "无此资源");
        }
    }
}
