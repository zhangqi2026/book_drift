package com.book_drift.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户勋章表 实体类
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_medal")
public class UserMedal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户 ID（关联 sys_user.id）
     */
    private Integer userId;

    /**
     * 勋章名称（如：捐赠之星、借阅达人）
     */
    private String medalName;

    /**
     * 勋章类型：1-捐赠 2-借阅 3-分享笔记
     */
    private Integer medalType;

    /**
     * 所需数量（解锁条件）
     */
    private Integer requiredCount;

    /**
     * 勋章说明
     */
    private String description;

    /**
     * 解锁时间
     */

    private Date unlockTime;
}
