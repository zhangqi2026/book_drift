package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.Announcement;
import com.book_drift.mapper.AnnouncementMapper;
import com.book_drift.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public Page<Announcement> pageQuery(Integer pageNum, Integer pageSize, Integer isPublished) {
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        
        if (isPublished != null) {
            queryWrapper.eq("is_published", isPublished);
        }
        
        queryWrapper.orderByDesc("publish_time");
        
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }

    @Override
    public Announcement getById(Integer id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    public boolean save(Announcement announcement) {
        announcement.setPublishTime(new Date());
        return this.getBaseMapper().insert(announcement) > 0;
    }

    @Override
    public boolean update(Announcement announcement) {
        announcement.setUpdateTime(new Date());
        return this.getBaseMapper().updateById(announcement) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return this.getBaseMapper().deleteById(id) > 0;
    }
}
