package com.book_drift.service;

import java.util.List;

public interface AnnouncementReadService {

    List<Integer> getReadAnnouncementIds(Integer userId);

    boolean markAsRead(Integer announcementId, Integer userId);
}
