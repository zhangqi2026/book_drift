package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookNote;
import com.book_drift.service.BookNoteService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.BookNoteVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 读书笔记控制器
 * </p>
 */
@RestController
@RequestMapping("/bookNote")
@Api(tags = "读书笔记管理")
public class BookNoteController {

    @Resource
    private BookNoteService bookNoteService;

    /**
     * 分页查询笔记列表 - POST 方式
     * @param size 每页大小
     * @param current 当前页码
     * @param bookId 书籍 ID（可选）
     * @param userId 用户 ID（可选，用于标记是否已点赞）
     * @return 分页结果
     */
    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询笔记列表（POST 方式）")
    public BaseResult<Page<BookNoteVO>> pageQuery(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "书籍 ID（可选）", example = "1")
            @RequestParam(required = false) Integer bookId,
            
            @ApiParam(value = "用户 ID（可选，用于标记是否已点赞）", example = "1")
            @RequestParam(required = false) Integer userId) {

        Page<BookNoteVO> page = bookNoteService.pageQuery(current, size, bookId, userId);
        return BaseResult.ok("查询成功", page);
    }

    /**
     * 根据 ID 查询笔记详情
     * @param id 笔记 ID
     * @return 笔记信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 ID 查询笔记详情")
    public BaseResult<BookNoteVO> getById(@PathVariable Integer id) {
        BookNoteVO bookNote = bookNoteService.getById(id);
        if (bookNote == null) {
            return BaseResult.error("笔记不存在");
        }
        return BaseResult.ok("查询成功", bookNote);
    }

    /**
     * 新增笔记
     * @param bookNote 笔记信息
     * @return 是否成功
     */
    @PostMapping
    @ApiOperation("新增笔记")
    public BaseResult<Boolean> save(@RequestBody BookNote bookNote) {
        Integer score = bookNoteService.saveWithScore(bookNote);
        if (score != null) {
            return BaseResult.ok("新增成功", true).append("score", score);
        }
        return BaseResult.error("新增失败");
    }

    /**
     * 修改笔记
     * @param bookNote 笔记信息
     * @return 是否成功
     */
    @PutMapping
    @ApiOperation("修改笔记")
    public BaseResult<Boolean> update(@RequestBody BookNote bookNote) {
        boolean result = bookNoteService.update(bookNote);
        if (result) {
            return BaseResult.ok("修改成功", result);
        }
        return BaseResult.error("修改失败");
    }

    /**
     * 删除笔记
     * @param id 笔记 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除笔记")
    public BaseResult<Boolean> delete(@PathVariable Integer id) {
        boolean result = bookNoteService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", result);
        }
        return BaseResult.error("删除失败");
    }

    /**
     * 点赞笔记（带记录）
     * @param id 笔记 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    @PostMapping("/like/{id}")
    @ApiOperation("点赞笔记")
    public BaseResult<Boolean> likeNote(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        Integer score = bookNoteService.likeNoteWithRecordWithScore(id, userId);
        if (score != null) {
            return BaseResult.ok("点赞成功", true).append("score", score);
        }
        return BaseResult.error("您已经点过赞了");
    }

    /**
     * 取消点赞笔记（带记录）
     * @param id 笔记 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    @PostMapping("/unlike/{id}")
    @ApiOperation("取消点赞笔记")
    public BaseResult<Boolean> unlikeNote(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        Integer score = bookNoteService.unlikeNoteWithRecordWithScore(id, userId);
        if (score != null) {
            return BaseResult.ok("取消点赞成功", true).append("score", score);
        }
        return BaseResult.error("取消点赞失败");
    }
}
