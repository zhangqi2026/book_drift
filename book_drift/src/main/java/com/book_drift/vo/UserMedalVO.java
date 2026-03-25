package com.book_drift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 用户勋章 VO 类
 * </p>
 */
@Data
@ApiModel("用户勋章")
public class UserMedalVO {

    @ApiModelProperty(value = "主键 ID")
    private Integer id;

    @ApiModelProperty(value = "用户 ID")
    private Integer userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "勋章名称")
    private String medalName;

    @ApiModelProperty(value = "勋章类型：1-捐赠 2-借阅 3-分享笔记")
    private Integer medalType;

    @ApiModelProperty(value = "所需数量（解锁条件）")
    private Integer requiredCount;

    @ApiModelProperty(value = "勋章说明")
    private String description;

    @ApiModelProperty(value = "解锁时间")
    private String unlockTime;
}
