package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookTag;
import com.book_drift.service.BookTagService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.BookTagVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 书籍标签控制器
 */
@RestController
@RequestMapping("/bookTag")
@Api(tags = "书籍标签管理")
public class BookTagController {

    @Resource
    private BookTagService bookTagService;

    /**
     * 分页查询标签列表
     * @param size 每页大小
     * @param current 当前页码
     * @param tagName 标签名称（模糊查询）
     * @return 分页结果
     */
    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询标签列表")
    public BaseResult<Page<BookTagVO>> pageQuery(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "标签名称（模糊查询）", example = "文学")
            @RequestParam(required = false) String tagName) {

        Page<BookTagVO> page = bookTagService.pageQuery(current, size, tagName);
        return BaseResult.ok("查询成功", page);
    }

    /**
     * 获取所有标签
     * @return 标签列表
     */
    @GetMapping("/all")
    @ApiOperation("获取所有标签")
    public BaseResult<List<BookTagVO>> getAllTags() {
        List<BookTagVO> tags = bookTagService.getAllTags();
        return BaseResult.ok("查询成功", tags);
    }

    /**
     * 获取所有标签（兼容前端调用）
     * @return 标签列表
     */
    @PostMapping("/listAll")
    @ApiOperation("获取所有标签")
    public BaseResult<List<BookTagVO>> listAllTags() {
        List<BookTagVO> tags = bookTagService.getAllTags();
        return BaseResult.ok("查询成功", tags);
    }

    /**
     * 根据 ID 查询标签
     * @param id 标签 ID
     * @return 标签信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 ID 查询标签")
    public BaseResult<BookTagVO> getById(
            @ApiParam(value = "标签 ID", required = true, example = "1")
            @PathVariable Integer id) {

        BookTagVO tag = bookTagService.getById(id);
        if (tag == null) {
            return BaseResult.error("标签不存在");
        }
        return BaseResult.ok("查询成功", tag);
    }

    /**
     * 新增标签
     * @param bookTag 标签信息
     * @return 是否成功
     */
    @PostMapping
    @ApiOperation("新增标签")
    public BaseResult<Boolean> save(@RequestBody BookTag bookTag) {
        boolean result = bookTagService.save(bookTag);
        if (result) {
            return BaseResult.ok("新增成功", true);
        }
        return BaseResult.error("新增失败");
    }

    /**
     * 新增标签（兼容前端调用）
     * @param bookTag 标签信息
     * @return 是否成功
     */
    @PostMapping("/add")
    @ApiOperation("新增标签")
    public BaseResult<Boolean> add(@RequestBody BookTag bookTag) {
        boolean result = bookTagService.save(bookTag);
        if (result) {
            return BaseResult.ok("新增成功", true);
        }
        return BaseResult.error("新增失败");
    }

    /**
     * 修改标签
     * @param bookTag 标签信息
     * @return 是否成功
     */
    @PutMapping
    @ApiOperation("修改标签")
    public BaseResult<Boolean> update(@RequestBody BookTag bookTag) {
        boolean result = bookTagService.update(bookTag);
        if (result) {
            return BaseResult.ok("修改成功", true);
        }
        return BaseResult.error("修改失败");
    }

    /**
     * 修改标签（兼容前端调用）
     * @param bookTag 标签信息
     * @return 是否成功
     */
    @PostMapping("/update")
    @ApiOperation("修改标签")
    public BaseResult<Boolean> updateTag(@RequestBody BookTag bookTag) {
        boolean result = bookTagService.update(bookTag);
        if (result) {
            return BaseResult.ok("修改成功", true);
        }
        return BaseResult.error("修改失败");
    }

    /**
     * 删除标签
     * @param id 标签 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除标签")
    public BaseResult<Boolean> delete(
            @ApiParam(value = "标签 ID", required = true, example = "1")
            @PathVariable Integer id) {

        boolean result = bookTagService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", true);
        }
        return BaseResult.error("删除失败");
    }

    /**
     * 删除标签（兼容前端调用）
     * @param id 标签 ID
     * @return 是否成功
     */
    @PostMapping("/delete")
    @ApiOperation("删除标签")
    public BaseResult<Boolean> deleteTag(@RequestParam Integer id) {
        boolean result = bookTagService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", true);
        }
        return BaseResult.error("删除失败");
    }

    /**
     * 给书籍绑定标签
     * @param bookId 书籍 ID
     * @param tagIds 标签 ID 列表
     * @return 是否成功
     */
    @PostMapping("/bind/{bookId}")
    @ApiOperation("给书籍绑定标签")
    public BaseResult<Boolean> bindTagsToBook(
            @ApiParam(value = "书籍 ID", required = true, example = "1")
            @PathVariable Integer bookId,

            @ApiParam(value = "标签 ID 列表", required = true)
            @RequestBody List<Integer> tagIds) {

        boolean result = bookTagService.bindTagsToBook(bookId, tagIds);
        if (result) {
            return BaseResult.ok("绑定成功", true);
        }
        return BaseResult.error("绑定失败");
    }

    /**
     * 给书籍绑定标签（兼容前端调用）
     * @param bookId 书籍 ID
     * @param tagIds 标签 ID 列表（逗号分隔）
     * @return 是否成功
     */
    @PostMapping("/bindBookTags")
    @ApiOperation("给书籍绑定标签")
    public BaseResult<Boolean> bindBookTags(
            @ApiParam(value = "书籍 ID", required = true, example = "1")
            @RequestParam Integer bookId,

            @ApiParam(value = "标签 ID 列表", required = true)
            @RequestParam String tagIds) {
        
        List<Integer> tagIdList = new ArrayList<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            String[] ids = tagIds.split(",");
            for (String id : ids) {
                tagIdList.add(Integer.parseInt(id.trim()));
            }
        }

        boolean result = bookTagService.bindTagsToBook(bookId, tagIdList);
        if (result) {
            return BaseResult.ok("绑定成功", true);
        }
        return BaseResult.error("绑定失败");
    }

    /**
     * 获取书籍的标签列表
     * @param bookId 书籍 ID
     * @return 标签列表
     */
    @GetMapping("/book/{bookId}")
    @ApiOperation("获取书籍的标签列表")
    public BaseResult<List<BookTagVO>> getBookTags(
            @ApiParam(value = "书籍 ID", required = true, example = "1")
            @PathVariable Integer bookId) {

        List<BookTagVO> tags = bookTagService.getBookTags(bookId);
        return BaseResult.ok("查询成功", tags);
    }

    /**
     * 根据标签 ID 列表筛选书籍
     * @param size 每页大小
     * @param current 当前页码
     * @param tagIds 标签 ID 列表
     * @return 分页的书籍 ID 列表
     */
    @PostMapping("/filter/{size}/{current}")
    @ApiOperation("根据标签筛选书籍")
    public BaseResult<Page<Integer>> filterBooksByTags(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "标签 ID 列表", required = true)
            @RequestBody List<Integer> tagIds) {

        Page<Integer> page = bookTagService.getBookIdsByTags(tagIds, current, size);
        return BaseResult.ok("查询成功", page);
    }
}
