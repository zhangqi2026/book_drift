package com.book_drift.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.book_drift.domain.BookStatistics;
import java.util.List;

public interface BookStatisticsService extends IService<BookStatistics> {
    void collectStatistics();
    List<BookStatistics> getRecentStatistics(int days);
    BookStatistics getLatestStatistics();
    BookStatistics getBookStatusStatistics();
}