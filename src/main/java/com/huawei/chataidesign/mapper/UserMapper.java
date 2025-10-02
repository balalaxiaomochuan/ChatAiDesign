package com.huawei.chataidesign.mapper;

import com.huawei.chataidesign.entity.User;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【user(用户信息表)】的数据库操作Mapper
* @createDate 2025-10-01 12:40:03
* @Entity com.huawei.chataidesign.entity.User
*/
@Mapper
public interface UserMapper {
    /**
     * 按username检索用户信息
     * @param username
     * @return user实体实例
     */
    User getUserByUserName(@Param("username") String username);

    int insert(@NotNull User userRegisterReq);
}




