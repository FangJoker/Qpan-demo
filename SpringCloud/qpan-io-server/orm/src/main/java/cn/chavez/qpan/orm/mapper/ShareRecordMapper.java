package cn.chavez.qpan.orm.mapper;

import cn.chavez.qpan.model.file.po.ShareRecordsPo;
import cn.chavez.qpan.model.file.vo.SharedFileVo;
import cn.chavez.qpan.model.user.vo.user.ShareFileListVo;
import cn.chavez.qpan.model.user.vo.user.ShareFileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/13 0:23
 */
@Mapper
@Repository
public interface ShareRecordMapper {

    /**
     * 查询记录
     * @param uuid
     * @return
     */
    ShareRecordsPo selectByFileUuid(@Param("uuid") String uuid);
    /**
     * 插入新记录
     *
     * @param shareRecordsPo
     * @return
     */
    int insertShareRecord(ShareRecordsPo shareRecordsPo);

    /**
     * 查询分享的文件列表
     *
     * @param key
     * @return
     */
    List<SharedFileVo> selectSharedResult(@Param("key")  String key);

    /**
     * 查询分享的文件
     *
     * @param uuid
     * @param passWord
     * @return
     */
    String selectShareFileUuidByShareRecordUuidAndPassWord(@Param("uuid") String uuid, @Param("passWord") String passWord);

    /**
     * 查询用户分享的文件列表
     * @return
     */
    List<ShareFileListVo> selectSharedFileList();
}
