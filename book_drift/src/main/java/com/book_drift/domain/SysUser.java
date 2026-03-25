package com.book_drift.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 系统用户表 实体类
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（自增，初学者易操作）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学号（唯一，扫码认领身份验证）
     */
    private String studentId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 学院（区分本校学生）
     */
    private String college;

    /**
     * 登录密码（简单加密即可）
     */
    private String password;

    /**
     * 角色：1-管理员 2-普通用户
     */
    private Integer role;

    /**
     * 累计借书次数
     */
    private Integer borrowCount;

    /**
     * 当前佩戴的勋章 ID（关联 user_medal.id）
     */
    private Integer currentMedalId;
}
