package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.Announcement;

public interface AnnouncementService {

    Page<Announcement> pageQuery(Integer pageNum, Integer pageSize, Integer isPublished);

    Announcement getById(Integer id);

    boolean save(Announcement announcement);

    boolean update(Announcement announcement);

    boolean delete(Integer id);
}
