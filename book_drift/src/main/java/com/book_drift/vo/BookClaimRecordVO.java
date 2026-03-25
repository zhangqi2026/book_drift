package com.book_drift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 书籍认领记录 VO 类
 * </p>
 */
@Data
@ApiModel("书籍认领记录")
public class BookClaimRecordVO {

    @ApiModelProperty(value = "主键 ID")
    private Integer id;

    @ApiModelProperty(value = "书籍 ID")
    private Integer bookId;

    @ApiModelProperty(value = "认领人 ID")
    private Integer userId;

    @ApiModelProperty(value = "认领时间", example = "2026-03-19")
    private String claimTime;

    @ApiModelProperty(value = "归还时间", example = "2026-04-18")
    private String returnTime;

    @ApiModelProperty(value = "应归还时间", example = "2026-04-18")
    private String dueTime;

    @ApiModelProperty(value = "是否超期：0-否 1-是")
    private Integer isOverdue;
    
    // 以下字段用于展示书籍信息
    @ApiModelProperty(value = "书籍名称")
    private String bookName;
    
    @ApiModelProperty(value = "作者")
    private String author;
    
    @ApiModelProperty(value = "捐赠人姓名")
    private String donorName;
    
    @ApiModelProperty(value = "认领人姓名/借阅人姓名")
    private String claimerName;
    
    // 兼容旧版本的字段名
    public String getUserName() {
        return this.claimerName;
    }
    
    public void setUserName(String userName) {
        this.claimerName = userName;
    }
    
    @ApiModelProperty(value = "书籍状态：1-待认领 2-已认领 3-已归还")
    private Integer bookStatus;
}
