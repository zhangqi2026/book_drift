package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.UserActivityRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户活跃度记录 Mapper 接口
 */
@Mapper
public interface UserActivityRecordMapper extends BaseMapper<UserActivityRecord> {
}
