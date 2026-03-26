package com.book_drift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 系统用户 VO 类
 * </p>
 */
@Data
@ApiModel("系统用户")
public class SysUserVO {

    @ApiModelProperty(value = "主键 ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "学号", example = "2024001")
    private String studentId;

    @ApiModelProperty(value = "学生姓名", example = "张三")
    private String name;

    @ApiModelProperty(value = "学院", example = "计算机学院")
    private String college;

    @ApiModelProperty(value = "角色：1-管理员 2-普通用户", example = "2")
    private Integer role;

    @ApiModelProperty(value = "登录密码")
    private String password;

    @ApiModelProperty(value = "累计借书次数", example = "10")
    private Integer borrowCount;

    @ApiModelProperty(value = "当前佩戴的勋章 ID", example = "1")
    private Integer currentMedalId;
    
    @ApiModelProperty(value = "总活跃度分数", example = "100")
    private Integer activityScore;
    
    @ApiModelProperty(value = "日活跃度分数", example = "20")
    private Integer dailyActivityScore;
    
    @ApiModelProperty(value = "周活跃度分数", example = "50")
    private Integer weeklyActivityScore;
    
    @ApiModelProperty(value = "月活跃度分数", example = "80")
    private Integer monthlyActivityScore;
    
    @ApiModelProperty(value = "排名（仅在排行榜中使用）", example = "1")
    private Integer rank;
}
