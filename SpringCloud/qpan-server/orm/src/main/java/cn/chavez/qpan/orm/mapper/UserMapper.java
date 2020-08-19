package cn.chavez.qpan.orm.mapper;

import cn.chavez.qpan.model.user.po.UserPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.springframework.stereotype.Repository;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 19:05
 */
@Mapper
@Repository
public interface UserMapper {

    /**
     * insert
     *
     * @param po
     * @return
     */
    int insert(UserPo po);

    /**
     * delete
     *
     * @param uuid
     * @return
     */
    int deleteByUuid(@Param("uuid") String uuid);

    /**
     * update free Bytes
     * @param uuid
     * @param freeBytes
     * @return
     */
    int updateFreeBytesByUuid(@Param("freeBytes") Long freeBytes,@Param("uuid") String uuid);

    /**
     * select Free Bytes
     *
     * @param uuid
     * @return
     */
    Long selectFreeBytesByUuid(@Param("uuid") String uuid);

    /**
     * select by account
     * @param account
     * @return
     */
    UserPo selectByAccount(@Param("account") String account);

    /**
     * 查询个人磁盘目录
     * @param uuid user_uuid
     * @return
     */
    String selectIndividualDir(@Param("uuid") String uuid);

    /**
     * 更新总容量
     * @param uuid
     * @param totalBytes
     * @return
     */
    int updateTotalBytesByUuid(@Param("uuid")String uuid, @Param("totalBytes")Long totalBytes);
}
