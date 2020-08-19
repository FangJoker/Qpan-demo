package cn.chavez.qpan.orm.mapper;

import cn.chavez.qpan.model.file.po.FileLinkPo;
import cn.chavez.qpan.model.file.vo.SharedFileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/12 18:01
 */
@Mapper
@Repository
public interface FileLinkMapper {
    /**
     * 新增记录
     *
     * @param fileLinkPo
     * @return
     */
    int insertNewFileLink(FileLinkPo fileLinkPo);

    /**
     * 更新linl
     * @param uuid
     * @param link
     * @return
     */
    int updateLinkByUuid (@Param("uuid") String uuid, @Param("link") String link);

    /**
     * 删除记录
     *
     * @param uuid
     * @param isDeleted 0-否 1-是
     * @return
     */
    int deleteFileLink(@Param("uuid") String uuid, @Param("isDeleted") int isDeleted);

    /**
     * 更新分享链接状态
     * @param uuid
     * @param status
     * @return
     */
    int updateShareFileListStatus(@Param("uuid") String uuid,@Param("status") int status);


}
