package com.book_drift.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 书籍标签实体类
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("book_tag")
public class BookTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签描述
     */
    private String tagDesc;
    
    /**
     * 标签描述（兼容前端，不映射到数据库）
     */
    @TableField(exist = false)
    private String description;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 获取描述（优先返回description，如果为空则返回tagDesc）
     */
    public String getDescription() {
        if (this.description != null && !this.description.isEmpty()) {
            return this.description;
        }
        return this.tagDesc;
    }
    
    /**
     * 设置描述（同时设置description和tagDesc）
     */
    public void setDescription(String description) {
        this.description = description;
        this.tagDesc = description;
    }
}
