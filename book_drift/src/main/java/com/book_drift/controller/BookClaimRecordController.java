package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.service.BookClaimRecordService;
import com.book_drift.service.BookInfoService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.BookClaimRecordVO;
import com.book_drift.vo.BookInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/bookClaimRecord")
@Api(tags = "书籍认领记录管理")
public class BookClaimRecordController {

    @Resource
    private BookClaimRecordService bookClaimRecordService;
    
    @Resource
    private BookInfoService bookInfoService;

    @PostMapping("/claim/{bookId}/{userId}")
    @ApiOperation("认领书籍（借书）")
    public BaseResult<Boolean> claimBook(
            @ApiParam(value = "书籍 ID", required = true, example = "1")
            @PathVariable Integer bookId,
            @ApiParam(value = "用户 ID", required = true, example = "1")
            @PathVariable Integer userId) {
        
        try {
            Integer score = bookClaimRecordService.claimBookWithScore(bookId, userId);
            if (score != null) {
                return BaseResult.ok("借书成功", true).append("score", score);
            } else {
                return BaseResult.error("借书失败");
            }
        } catch (RuntimeException e) {
            return BaseResult.error(e.getMessage());
        }
    }

    @PostMapping("/return/{recordId}")
    @ApiOperation("归还书籍")
    public BaseResult<Boolean> returnBook(@PathVariable Integer recordId) {
        try {
            Integer score = bookClaimRecordService.returnBookWithScore(recordId);
            if (score != null) {
                return BaseResult.ok("还书成功", true).append("score", score);
            } else {
                return BaseResult.error("还书失败");
            }
        } catch (RuntimeException e) {
            return BaseResult.error(e.getMessage());
        }
    }

    @GetMapping("/history/{bookId}")
    @ApiOperation("查询书籍漂流轨迹")
    public BaseResult<Page<BookClaimRecordVO>> getHistory(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "当前页码", required = true, example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "书籍 ID", required = true, example = "1")
            @PathVariable Integer bookId) {
        
        Page<BookClaimRecordVO> page = bookClaimRecordService.pageQueryByBookId(current, size, bookId);
        return BaseResult.ok("查询成功", page);
    }

    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询用户借阅记录")
    public BaseResult<Page<BookClaimRecordVO>> pageQuery(
            @PathVariable("size") int size,
            @PathVariable("current") int current,
            @RequestParam(required = false) Integer userId) {

        Page<BookClaimRecordVO> page = bookClaimRecordService.pageQuery(current, size, userId);
        return BaseResult.ok("查询成功", page);
    }
    
    @PostMapping("/scan/claim/{bookQrcode}/{userId}")
    @ApiOperation("扫码借书")
    public BaseResult<Boolean> scanClaimBook(
            @PathVariable String bookQrcode,
            @PathVariable Integer userId) {
        try {
            BookInfoVO bookVO = bookInfoService.getByQrcode(bookQrcode);
            if (bookVO == null) {
                return BaseResult.error("书籍不存在");
            }
            Integer score = bookClaimRecordService.claimBookWithScore(bookVO.getId(), userId);
            if (score != null) {
                return BaseResult.ok("借书成功", true).append("score", score).append("bookId", bookVO.getId());
            } else {
                return BaseResult.error("借书失败");
            }
        } catch (RuntimeException e) {
            return BaseResult.error(e.getMessage());
        }
    }
    
    @PostMapping("/scan/return/{bookQrcode}/{userId}")
    @ApiOperation("扫码还书")
    public BaseResult<Boolean> scanReturnBook(
            @PathVariable String bookQrcode,
            @PathVariable Integer userId) {
        try {
            BookInfoVO bookVO = bookInfoService.getByQrcode(bookQrcode);
            if (bookVO == null) {
                return BaseResult.error("书籍不存在");
            }
            if (bookVO.getBookStatus() != 2) {
                return BaseResult.error("该书籍未被借阅");
            }
            if (bookVO.getCurrentHolderId() == null || !bookVO.getCurrentHolderId().equals(userId)) {
                return BaseResult.error("当前用户不是书籍持有者");
            }
            Page<BookClaimRecordVO> page = bookClaimRecordService.pageQueryByBookId(1, 1, bookVO.getId());
            if (page.getRecords() == null || page.getRecords().isEmpty()) {
                return BaseResult.error("没有找到借阅记录");
            }
            Integer recordId = page.getRecords().get(0).getId();
            Integer score = bookClaimRecordService.returnBookWithScore(recordId);
            if (score != null) {
                return BaseResult.ok("还书成功", true).append("score", score);
            } else {
                return BaseResult.error("还书失败");
            }
        } catch (RuntimeException e) {
            return BaseResult.error(e.getMessage());
        }
    }
}
