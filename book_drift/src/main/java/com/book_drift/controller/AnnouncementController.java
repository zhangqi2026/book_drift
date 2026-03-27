package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.Announcement;
import com.book_drift.service.AnnouncementService;
import com.book_drift.vo.BaseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/announcement")
@Api(tags = "公告管理")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询公告列表")
    public BaseResult<Page<Announcement>> pageQuery(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,
            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,
            @ApiParam(value = "是否发布：0-草稿 1-已发布（可选）", example = "1")
            @RequestParam(required = false) Integer isPublished) {
        Page<Announcement> page = announcementService.pageQuery(current, size, isPublished);
        return BaseResult.ok("查询成功", page);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据 ID 查询公告详情")
    public BaseResult<Announcement> getById(@PathVariable Integer id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return BaseResult.error("公告不存在");
        }
        return BaseResult.ok("查询成功", announcement);
    }

    @PostMapping
    @ApiOperation("新增公告")
    public BaseResult<Boolean> save(@RequestBody Announcement announcement) {
        boolean result = announcementService.save(announcement);
        if (result) {
            return BaseResult.ok("发布成功", result);
        }
        return BaseResult.error("发布失败");
    }

    @PutMapping
    @ApiOperation("修改公告")
    public BaseResult<Boolean> update(@RequestBody Announcement announcement) {
        boolean result = announcementService.update(announcement);
        if (result) {
            return BaseResult.ok("修改成功", result);
        }
        return BaseResult.error("修改失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除公告")
    public BaseResult<Boolean> delete(@PathVariable Integer id) {
        boolean result = announcementService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", result);
        }
        return BaseResult.error("删除失败");
    }
}
