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
 * 用户活跃度记录表 实体类
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_activity_record")
public class UserActivityRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户 ID
     */
    private Integer userId;

    /**
     * 活动类型：1-发布书籍 2-成功借出 3-成功归还/认领 4-发布笔记 5-被点赞
     */
    private Integer activityType;

    /**
     * 增加的活跃度分数
     */
    private Integer score;

    /**
     * 相关业务 ID（书籍 ID、笔记 ID 等）
     */
    private Integer businessId;

    /**
     * 创建时间
     */
    private Date createTime;
}
