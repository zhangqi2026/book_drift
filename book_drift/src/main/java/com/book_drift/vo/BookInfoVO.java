package com.book_drift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 书籍信息 VO 类
 * </p>
 */
@Data
@ApiModel("书籍信息")
public class BookInfoVO {

    @ApiModelProperty(value = "主键 ID")
    private Integer id;

    @ApiModelProperty(value = "书籍专属二维码")
    private String bookQrcode;

    @ApiModelProperty(value = "书名")
    private String bookName;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "捐赠人 ID")
    private Integer donorId;
    
    @ApiModelProperty(value = "捐赠人姓名")
    private String donorName;

    @ApiModelProperty(value = "捐赠时间", example = "2026-03-19")
    private String donateTime;

    @ApiModelProperty(value = "当前持有者 ID")
    private Integer currentHolderId;

    @ApiModelProperty(value = "借阅到期时间", example = "2026-04-18")
    private String borrowDeadline;

    @ApiModelProperty(value = "状态：1-待认领 2-已认领 3-已归还")
    private Integer bookStatus;
}
