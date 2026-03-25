package com.book_drift.config;

import com.book_drift.service.BookStatisticsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import javax.annotation.Resource;

@Configuration
@EnableScheduling
public class StatisticsTaskConfig {
    @Resource
    private BookStatisticsService bookStatisticsService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void collectDailyStatistics() {
        bookStatisticsService.collectStatistics();
    }
}