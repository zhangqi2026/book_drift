package com.book_drift.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("book_statistics")
public class BookStatistics {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Date statDate;
    private Integer totalBooks;
    private Integer availableBooks;
    private Integer borrowedBooks;
    private Integer returnedBooks;
    private Integer totalUsers;
    private Integer totalBorrows;
    private Integer totalDonations;
    private Integer activeUsers;
    private Date createTime;
}