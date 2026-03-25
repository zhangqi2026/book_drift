package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookClaimRecord;
import com.book_drift.service.BookClaimRecordService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.BookClaimRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 书籍认领记录控制器
 * </p>
 */
@RestController
@RequestMapping("/bookClaimRecord")
@Api(tags = "书籍认领记录管理")
public class BookClaimRecordController {

    @Resource
    private BookClaimRecordService bookClaimRecordService;

    /**
     * 认领书籍（借书）
     * @param bookId 书籍 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    @PostMapping("/claim/{bookId}/{userId}")
    @ApiOperation("认领书籍（借书）")
    public BaseResult<Boolean> claimBook(
            @ApiParam(value = "书籍 ID", required = true, example = "1")
            @PathVariable Integer bookId,
            @ApiParam(value = "用户 ID", required = true, example = "1")
            @PathVariable Integer userId) {
        
        try {
            // 调用 Service 实现借书业务逻辑
            boolean result = bookClaimRecordService.claimBook(bookId, userId);
            if (result) {
                return BaseResult.ok("借书成功", true);
            } else {
                return BaseResult.error("借书失败");
            }
        } catch (RuntimeException e) {
            return BaseResult.error(e.getMessage());
        }
    }

    /**
     * 归还书籍
     * @param recordId 借书记录 ID
     * @return 是否成功
     */
    @PostMapping("/return/{recordId}")
    @ApiOperation("归还书籍")
    public BaseResult<Boolean> returnBook(@PathVariable Integer recordId) {
        try {
            // 调用 Service 实现还书业务逻辑
            boolean result = bookClaimRecordService.returnBook(recordId);
            if (result) {
                return BaseResult.ok("还书成功", true);
            } else {
                return BaseResult.error("还书失败");
            }
        } catch (RuntimeException e) {
            return BaseResult.error(e.getMessage());
        }
    }

    /**
     * 查询书籍的漂流轨迹（历史借阅记录）
     * @param bookId 书籍 ID
     * @return 历史记录列表
     */
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

    /**
     * 分页查询用户的借阅记录
     * @param size 每页大小
     * @param current 当前页码
     * @param userId 用户 ID（可选）
     * @return 分页结果
     */
    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询用户借阅记录")
    public BaseResult<Page<BookClaimRecordVO>> pageQuery(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "用户 ID（可选）", example = "1")
            @RequestParam(required = false) Integer userId) {

        Page<BookClaimRecordVO> page = bookClaimRecordService.pageQuery(current, size, userId);
        return BaseResult.ok("查询成功", page);
    }
}
