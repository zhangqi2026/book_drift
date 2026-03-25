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
}
