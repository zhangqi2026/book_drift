package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.service.UserActivityService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.SysUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户活跃度控制器
 */
@RestController
@RequestMapping("/userActivity")
@Api(tags = "用户活跃度管理")
public class UserActivityController {

    @Resource
    private UserActivityService userActivityService;

    /**
     * 获取排行榜
     * @param rankType 排行榜类型：daily-日榜 weekly-周榜 monthly-月榜 total-总榜
     * @param size 每页大小
     * @param current 当前页码
     * @return 分页结果
     */
    @PostMapping("/rank/{size}/{current}")
    @ApiOperation("获取排行榜")
    public BaseResult<Page<SysUserVO>> getRankList(
            @ApiParam(value = "排行榜类型", required = true, example = "total")
            @RequestParam String rankType,
            
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current) {

        Page<SysUserVO> page = userActivityService.getRankList(rankType, current, size);
        return BaseResult.ok("查询成功", page);
    }

    /**
     * 获取用户的排名
     * @param userId 用户 ID
     * @param rankType 排行榜类型
     * @return 排名信息
     */
    @GetMapping("/userRank/{userId}")
    @ApiOperation("获取用户的排名")
    public BaseResult<SysUserVO> getUserRank(
            @ApiParam(value = "用户 ID", required = true, example = "1")
            @PathVariable Integer userId,
            
            @ApiParam(value = "排行榜类型", required = true, example = "total")
            @RequestParam String rankType) {

        SysUserVO userRank = userActivityService.getUserRank(userId, rankType);
        if (userRank == null) {
            return BaseResult.error("用户不存在");
        }
        return BaseResult.ok("查询成功", userRank);
    }
}
