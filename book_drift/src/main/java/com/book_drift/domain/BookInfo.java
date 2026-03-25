package com.book_drift.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 书籍信息表 实体类
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("book_info")
public class BookInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 书籍专属二维码（唯一，扫码核心）
     */
    private String bookQrcode;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 作者（非必填，简化输入）
     */
    private String author;

    /**
     * 捐赠人 ID（关联 sys_user.id）
     */
    private Integer donorId;

    /**
     * 捐赠时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date donateTime;

    /**
     * 当前持有者 ID（NULL=未认领）
     */
    private Integer currentHolderId;

    /**
     * 借阅到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date borrowDeadline;

    /**
     * 状态：1-待认领 2-已认领 3-已归还
     */
    private Integer bookStatus;
}
