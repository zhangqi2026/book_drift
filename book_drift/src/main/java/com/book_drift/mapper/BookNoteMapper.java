package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.BookNote;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 读书笔记表 Mapper 接口
 * </p>
 */
@Mapper
public interface BookNoteMapper extends BaseMapper<BookNote> {

}
