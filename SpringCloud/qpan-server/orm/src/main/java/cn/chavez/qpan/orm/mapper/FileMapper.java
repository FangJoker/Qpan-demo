package cn.chavez.qpan.orm.mapper;

import cn.chavez.qpan.model.file.po.FilePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/6 15:54
 */
@Mapper
@Repository
public interface FileMapper {

    /**
     * 查询文件信息
     *
     * @param uuid 文件uuid
     * @return
     */
    FilePo selectFilePOByFileUuid(@Param("uuid") String uuid);

    /**
     * 插入文件信息
     *
     * @param filePO
     * @return
     */
    int insertFile(FilePo filePO);

    /**
     * 更新文件删除状态
     * @param uuid
     * @param isDeleted 0-否 1-是
     * @return
     */
    int updateDeleted (@Param("uuid") String uuid, @Param("isDeleted")int isDeleted);

    /**
     * 查询指定文件的uuid
     *
     * @param path
     * @return
     */
    String selectFileUuidByPath (@Param("path") String path);

    /**
     * 查询文件本地路径
     * @param uuid
     * @return
     */
    String selectPathByUuid(@Param("uuid") String uuid);

}
