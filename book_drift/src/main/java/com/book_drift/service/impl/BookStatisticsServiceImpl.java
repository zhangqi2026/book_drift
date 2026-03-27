package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.BookClaimRecord;
import com.book_drift.domain.BookInfo;
import com.book_drift.domain.BookStatistics;
import com.book_drift.domain.SysUser;
import com.book_drift.mapper.BookClaimRecordMapper;
import com.book_drift.mapper.BookInfoMapper;
import com.book_drift.mapper.BookStatisticsMapper;
import com.book_drift.mapper.SysUserMapper;
import com.book_drift.service.BookStatisticsService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

@Service
public class BookStatisticsServiceImpl extends ServiceImpl<BookStatisticsMapper, BookStatistics> implements BookStatisticsService {
    @Resource
    private BookInfoMapper bookInfoMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private BookClaimRecordMapper bookClaimRecordMapper;

    @Override
    public void collectStatistics() {
        Date statDate = new Date();

        QueryWrapper<BookStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("stat_date", statDate);
        BookStatistics existing = baseMapper.selectOne(queryWrapper);
        if (existing != null) {
            return;
        }

        BookStatistics statistics = new BookStatistics();
        statistics.setStatDate(statDate);

        QueryWrapper<BookInfo> bookQuery = new QueryWrapper<>();
        statistics.setTotalBooks(bookInfoMapper.selectCount(bookQuery));

        QueryWrapper<BookInfo> availableQuery = new QueryWrapper<>();
        availableQuery.eq("book_status", 1);
        statistics.setAvailableBooks(bookInfoMapper.selectCount(availableQuery));

        QueryWrapper<BookInfo> borrowedQuery = new QueryWrapper<>();
        borrowedQuery.eq("book_status", 2);
        statistics.setBorrowedBooks(bookInfoMapper.selectCount(borrowedQuery));

        QueryWrapper<BookInfo> returnedQuery = new QueryWrapper<>();
        returnedQuery.eq("book_status", 3);
        statistics.setReturnedBooks(bookInfoMapper.selectCount(returnedQuery));

        QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
        statistics.setTotalUsers(sysUserMapper.selectCount(userQuery));

        QueryWrapper<BookClaimRecord> borrowQuery = new QueryWrapper<>();
        statistics.setTotalBorrows(bookClaimRecordMapper.selectCount(borrowQuery));

        QueryWrapper<BookInfo> donationQuery = new QueryWrapper<>();
        donationQuery.isNotNull("donor_id");
        statistics.setTotalDonations(bookInfoMapper.selectCount(donationQuery));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(statDate);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = calendar.getTime();
        QueryWrapper<BookClaimRecord> activeQuery = new QueryWrapper<>();
        activeQuery.ge("claim_time", sevenDaysAgo);
        List<BookClaimRecord> activeRecords = bookClaimRecordMapper.selectList(activeQuery);
        long activeUserCount = activeRecords.stream().map(BookClaimRecord::getUserId).distinct().count();
        statistics.setActiveUsers((int) activeUserCount);

        baseMapper.insert(statistics);
    }

    @Override
    public BookStatistics getBookStatusStatistics() {
        BookStatistics statistics = new BookStatistics();
        
        QueryWrapper<BookInfo> bookQuery = new QueryWrapper<>();
        statistics.setTotalBooks(bookInfoMapper.selectCount(bookQuery));

        QueryWrapper<BookInfo> availableQuery = new QueryWrapper<>();
        availableQuery.eq("book_status", 1);
        statistics.setAvailableBooks(bookInfoMapper.selectCount(availableQuery));

        QueryWrapper<BookInfo> borrowedQuery = new QueryWrapper<>();
        borrowedQuery.eq("book_status", 2);
        statistics.setBorrowedBooks(bookInfoMapper.selectCount(borrowedQuery));

        QueryWrapper<BookInfo> returnedQuery = new QueryWrapper<>();
        returnedQuery.eq("book_status", 3);
        statistics.setReturnedBooks(bookInfoMapper.selectCount(returnedQuery));

        return statistics;
    }

    @Override
    public List<BookStatistics> getRecentStatistics(int days) {
        QueryWrapper<BookStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date").last("LIMIT " + days);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public BookStatistics getLatestStatistics() {
        QueryWrapper<BookStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date").last("LIMIT 1");
        return baseMapper.selectOne(queryWrapper);
    }
}