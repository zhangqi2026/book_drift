package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.BookTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 书籍标签 Mapper 接口
 * </p>
 */
@Mapper
public interface BookTagMapper extends BaseMapper<BookTag> {

}
