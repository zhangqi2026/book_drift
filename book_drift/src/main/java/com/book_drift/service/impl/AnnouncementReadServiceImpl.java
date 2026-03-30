package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.AnnouncementRead;
import com.book_drift.mapper.AnnouncementReadMapper;
import com.book_drift.service.AnnouncementReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AnnouncementReadServiceImpl extends ServiceImpl<AnnouncementReadMapper, AnnouncementRead> implements AnnouncementReadService {

    @Override
    public List<Integer> getReadAnnouncementIds(Integer userId) {
        return this.getBaseMapper().getReadAnnouncementIds(userId);
    }

    @Override
    public boolean markAsRead(Integer announcementId, Integer userId) {
        QueryWrapper<AnnouncementRead> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("announcement_id", announcementId)
                .eq("user_id", userId);
        
        AnnouncementRead existing = this.getBaseMapper().selectOne(queryWrapper);
        if (existing != null) {
            return true;
        }
        
        AnnouncementRead announcementRead = new AnnouncementRead();
        announcementRead.setAnnouncementId(announcementId);
        announcementRead.setUserId(userId);
        announcementRead.setReadTime(new Date());
        
        return this.getBaseMapper().insert(announcementRead) > 0;
    }

    @Override
    public Integer countUnreadAnnouncements(Integer userId) {
        Integer count = this.getBaseMapper().countUnreadAnnouncements(userId);
        return count != null ? count : 0;
    }
}
