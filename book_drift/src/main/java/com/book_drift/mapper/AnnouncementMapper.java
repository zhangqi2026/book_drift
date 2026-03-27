package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
