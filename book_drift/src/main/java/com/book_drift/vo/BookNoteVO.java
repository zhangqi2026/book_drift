package com.book_drift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 读书笔记 VO 类
 * </p>
 */
@Data
@ApiModel("读书笔记")
public class BookNoteVO {

    @ApiModelProperty(value = "主键 ID")
    private Integer id;

    @ApiModelProperty(value = "书籍 ID")
    private Integer bookId;

    @ApiModelProperty(value = "笔记作者 ID")
    private Integer userId;

    @ApiModelProperty(value = "笔记作者名")
    private String userName;

    @ApiModelProperty(value = "笔记内容")
    private String noteContent;

    @ApiModelProperty(value = "标注重点段落")
    private String markParagraph;

    @ApiModelProperty(value = "点赞数", example = "0")
    private Integer likeCount;

    @ApiModelProperty(value = "创建时间", example = "2026-03-19")
    private String createTime;

    @ApiModelProperty(value = "当前用户是否已点赞")
    private Boolean isLiked;
}
