package com.book_drift.controller;

import com.book_drift.service.AnnouncementReadService;
import com.book_drift.vo.BaseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/announcementRead")
@Api(tags = "公告阅读管理")
public class AnnouncementReadController {

    @Resource
    private AnnouncementReadService announcementReadService;

    @GetMapping("/readIds/{userId}")
    @ApiOperation("获取用户已读的公告ID列表")
    public BaseResult<List<Integer>> getReadAnnouncementIds(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Integer userId) {
        List<Integer> readIds = announcementReadService.getReadAnnouncementIds(userId);
        return BaseResult.ok("查询成功", readIds);
    }

    @PostMapping("/markRead")
    @ApiOperation("标记公告为已读")
    public BaseResult<Boolean> markAsRead(
            @ApiParam(value = "公告ID", required = true)
            @RequestParam Integer announcementId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam Integer userId) {
        boolean result = announcementReadService.markAsRead(announcementId, userId);
        if (result) {
            return BaseResult.ok("标记成功", result);
        }
        return BaseResult.error("标记失败");
    }
}
