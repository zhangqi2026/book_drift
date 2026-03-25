package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.BookNoteLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 读书笔记点赞记录 Mapper 接口
 * </p>
 */
@Mapper
public interface BookNoteLikeMapper extends BaseMapper<BookNoteLike> {

}
