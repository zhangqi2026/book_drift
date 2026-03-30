package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.AnnouncementRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {

    @Select("SELECT announcement_id FROM announcement_read WHERE user_id = #{userId}")
    List<Integer> getReadAnnouncementIds(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM announcement a WHERE a.is_published = 1 " +
            "AND a.id NOT IN (SELECT ar.announcement_id FROM announcement_read ar WHERE ar.user_id = #{userId})")
    Integer countUnreadAnnouncements(@Param("userId") Integer userId);
}
