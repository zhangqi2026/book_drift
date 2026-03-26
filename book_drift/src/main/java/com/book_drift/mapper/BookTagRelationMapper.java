package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.BookTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 书籍标签关联表 Mapper 接口
 * </p>
 */
@Mapper
public interface BookTagRelationMapper extends BaseMapper<BookTagRelation> {

}
