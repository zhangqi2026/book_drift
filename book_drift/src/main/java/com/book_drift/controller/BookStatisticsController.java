package com.book_drift.controller;

import com.book_drift.domain.BookStatistics;
import com.book_drift.service.BookStatisticsService;
import com.book_drift.vo.BaseResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/bookStatistics")
public class BookStatisticsController {
    @Resource
    private BookStatisticsService bookStatisticsService;

    @PostMapping("/collect")
    public BaseResult<?> collectStatistics() {
        bookStatisticsService.collectStatistics();
        return BaseResult.ok("统计数据收集成功");
    }

    @PostMapping("/recent/{days}")
    public BaseResult<List<BookStatistics>> getRecentStatistics(@PathVariable int days) {
        List<BookStatistics> statistics = bookStatisticsService.getRecentStatistics(days);
        return BaseResult.ok("获取统计数据成功", statistics);
    }

    @PostMapping("/latest")
    public BaseResult<BookStatistics> getLatestStatistics() {
        BookStatistics statistics = bookStatisticsService.getLatestStatistics();
        return BaseResult.ok("获取最新统计数据成功", statistics);
    }
}