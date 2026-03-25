package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.BookClaimRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 书籍认领/归还记录表 Mapper 接口
 * </p>
 */
@Mapper
public interface BookClaimRecordMapper extends BaseMapper<BookClaimRecord> {

}
