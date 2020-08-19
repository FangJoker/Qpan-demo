package cn.chavez.qpan.user.service.impl;

import cn.chavez.qpan.model.file.po.FileLinkPo;
import cn.chavez.qpan.model.file.po.FilePo;
import cn.chavez.qpan.model.file.po.ShareRecordsPo;
import cn.chavez.qpan.model.file.vo.FileItemVo;
import cn.chavez.qpan.model.file.vo.SharedFileVo;
import cn.chavez.qpan.model.user.vo.user.ShareFileVo;
import cn.chavez.qpan.orm.mapper.FileLinkMapper;
import cn.chavez.qpan.orm.mapper.FileMapper;
import cn.chavez.qpan.orm.mapper.ShareRecordMapper;
import cn.chavez.qpan.orm.mapper.UserMapper;
import cn.chavez.qpan.support.ResponseEntitySupport;
import cn.chavez.qpan.support.date.DateSupport;
import cn.chavez.qpan.support.file.BaseFileTo;
import cn.chavez.qpan.support.file.FileManagerSupport;
import cn.chavez.qpan.support.jwt.JWTokenSupport;
import cn.chavez.qpan.user.service.FileDataService;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.util.TextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/3/21 1:31
 */
@Service
public class FileDataServiceImpl implements FileDataService {
    @Resource
    UserMapper userMapper;
    @Resource
    FileMapper fileMapper;
    @Resource
    FileLinkMapper fileLinkMapper;
    @Resource
    ShareRecordMapper shareRecordMapper;

    @Override
    public ResponseEntity<Object> queryFileListByUserUuid(String pathUuid) {
        String userUuid = JWTokenSupport.getJWTokenUuid();
        List<FileItemVo> resultList = new ArrayList<>();
        List<BaseFileTo> fileToList;
        String individualRootDir = userMapper.selectIndividualDir(userUuid);
        if (!TextUtils.isBlank(pathUuid)) {
            // 进入指定目录需要判断
            String path = fileMapper.selectPathByUuid(pathUuid);
            if (path == null) {
                return ResponseEntitySupport.error(HttpStatus.NOT_FOUND, "无此资源", null);
            } else if (FileManagerSupport.isBelongToRoot(path, individualRootDir)) {
                fileToList = FileManagerSupport.getFileList(path);
            } else {
                return ResponseEntitySupport.error(HttpStatus.FORBIDDEN, "无访问权限", null);
            }
        } else {
            fileToList = FileManagerSupport.getFileList(individualRootDir);
        }
        transportFileVo(fileToList, resultList);
        JSONObject responseJson = new JSONObject();
        responseJson.put("file_list", resultList);
        return ResponseEntitySupport.success(responseJson);
    }

    @Override
    public ResponseEntity<Object> searchTarget(String target) {
        String userUuid = JWTokenSupport.getJWTokenUuid();
        List<BaseFileTo> result = new ArrayList<>();
        List<FileItemVo> responseList = new ArrayList<>();
        String individualRootDir = userMapper.selectIndividualDir(userUuid);
        FileManagerSupport.searchFileInRoot(individualRootDir, target, result);
        transportFileVo(result, responseList);
        JSONObject resultJson = new JSONObject();
        resultJson.put("result", result);
        return ResponseEntitySupport.success(resultJson);
    }

    @Override
    public ResponseEntity<Object> shareFile(ShareFileVo body) {
        String userUuid = JWTokenSupport.getJWTokenUuid();
        List<String> fileUuids = body.getUuids();
        final String key =UUID.randomUUID().toString().substring(0, 6).replaceAll("-", "");
        for (String uuid : fileUuids) {
            ShareRecordsPo oldShareRecordsPo = shareRecordMapper.selectByFileUuid(uuid);
            if (oldShareRecordsPo!=null){
                //存在记录更新link
                if(fileLinkMapper.updateLinkByUuid(oldShareRecordsPo.getUuid(),key)==1){
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("key",key);
                    return ResponseEntitySupport.success(responseJson);
                }else {
                    return  ResponseEntitySupport.error(HttpStatus.CONFLICT,null,null);
                }
            }
            FilePo filePO = fileMapper.selectFilePOByFileUuid(uuid);
            FileLinkPo fileLinkPo = new FileLinkPo();
            if (filePO != null) {
                fileLinkPo.setFileUuid(filePO.getUuid());
                fileLinkPo.setPassword(body.getPassWord());
                fileLinkPo.setStatus(0);
                // 1天后过期
                long validityTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
                fileLinkPo.setValidityDate(DateSupport.getSimpleDateString(new Date(validityTime)));
                fileLinkPo.setLink(key);
                fileLinkMapper.insertNewFileLink(fileLinkPo);

                ShareRecordsPo shareRecordsPo = new ShareRecordsPo();
                shareRecordsPo.setFileUuid(filePO.getUuid());
                shareRecordsPo.setLinkUuid(fileLinkPo.getUuid());
                shareRecordsPo.setUserUuid(userUuid);
                shareRecordMapper.insertShareRecord(shareRecordsPo);
            }
        }
        JSONObject responseJson = new JSONObject();
        responseJson.put("key",key);
        return ResponseEntitySupport.success(responseJson);
    }

    @Override
    public ResponseEntity<Object> searchShareFile(String key) {
        List<SharedFileVo> result = shareRecordMapper.selectSharedResult(key);
        return  ResponseEntitySupport.success(result);
    }

    @Override
    public ResponseEntity<Object> shareFileList() {
        return  ResponseEntitySupport.success(shareRecordMapper.selectSharedFileList());
    }

    @Override
    public ResponseEntity<Object> updateShareFileListStatus(String uuid,int status) {
        return ResponseEntitySupport.success(fileLinkMapper.updateShareFileListStatus(uuid,status));
    }

    private void transportFileVo(List<BaseFileTo> baseFileToList, List<FileItemVo> fileItemVoList) {
        for (BaseFileTo baseFileTo : baseFileToList) {
            String fileUuid = fileMapper.selectFileUuidByPath(baseFileTo.getPath());
            if (fileUuid != null) {
                FileItemVo fileItemVo = new FileItemVo();
                fileItemVo.setUuid(fileUuid);
                fileItemVo.setLastModified(baseFileTo.getLastModified());
                fileItemVo.setName((baseFileTo.getName()));
                fileItemVo.setSize((baseFileTo.getSize()));
                fileItemVo.setType((baseFileTo.getType()));
                fileItemVoList.add(fileItemVo);
            }
        }
    }
}
