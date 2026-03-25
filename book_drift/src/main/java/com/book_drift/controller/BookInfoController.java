package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookInfo;
import com.book_drift.service.BookInfoService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.BookInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 书籍信息控制器
 * </p>
 */
@RestController
@RequestMapping("/bookInfo")
@Api(tags = "书籍信息管理")
public class BookInfoController {

    @Resource
    private BookInfoService bookInfoService;

    /**
     * 分页查询书籍列表 - POST 方式
     * @param size 每页大小
     * @param current 当前页码
     * @param bookName 书名（可选，模糊查询）
     * @param donorId 捐赠人 ID（可选）
     * @return 分页结果
     */
    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询书籍列表（POST 方式）")
    public BaseResult<Page<BookInfoVO>> pageQuery(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "书名（可选，模糊查询）", example = "Java")
            @RequestParam(required = false) String bookName,
            
            @ApiParam(value = "捐赠人 ID（可选）", example = "1")
            @RequestParam(required = false) Integer donorId) {

        Page<BookInfoVO> page = bookInfoService.pageQuery(current, size, bookName, donorId);
        return BaseResult.ok("查询成功", page);
    }

    /**
     * 根据 ID 查询书籍详情
     * @param id 书籍 ID
     * @return 书籍信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 ID 查询书籍详情")
    public BaseResult<BookInfoVO> getById(@PathVariable Integer id) {
        BookInfoVO bookInfo = bookInfoService.getById(id);
        if (bookInfo == null) {
            return BaseResult.error("书籍不存在");
        }
        return BaseResult.ok("查询成功", bookInfo);
    }

    /**
     * 新增书籍
     * @param bookInfo 书籍信息
     * @return 是否成功
     */
    @PostMapping
    @ApiOperation("新增书籍")
    public BaseResult<Boolean> save(@RequestBody BookInfo bookInfo) {
        boolean result = bookInfoService.save(bookInfo);
        if (result) {
            return BaseResult.ok("新增成功", result);
        }
        return BaseResult.error("新增失败");
    }

    /**
     * 修改书籍
     * @param bookInfo 书籍信息
     * @return 是否成功
     */
    @PutMapping
    @ApiOperation("修改书籍")
    public BaseResult<Boolean> update(@RequestBody BookInfo bookInfo) {
        boolean result = bookInfoService.update(bookInfo);
        if (result) {
            return BaseResult.ok("修改成功", result);
        }
        return BaseResult.error("修改失败");
    }

    /**
     * 删除书籍
     * @param id 书籍 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除书籍")
    public BaseResult<Boolean> delete(@PathVariable Integer id) {
        boolean result = bookInfoService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", result);
        }
        return BaseResult.error("删除失败");
    }
}
