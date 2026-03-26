package com.book_drift.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 书籍标签 VO
 */
@Data
public class BookTagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
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
     * 标签描述（兼容前端）
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 关联的书籍数量
     */
    private Integer bookCount;
    
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
