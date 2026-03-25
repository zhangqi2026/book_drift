package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 学生用户表 Mapper 接口
 * </p>
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据 ID 查询用户信息
     * @param userId 用户 ID
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE id = #{userId}")
    SysUser getUserById(Integer userId);
}
