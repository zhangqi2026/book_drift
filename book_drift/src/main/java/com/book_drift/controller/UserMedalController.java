package com.book_drift.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.SysUser;
import com.book_drift.domain.UserMedal;
import com.book_drift.mapper.SysUserMapper;
import com.book_drift.service.UserMedalService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.UserMedalVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户勋章控制器
 * </p>
 */
@RestController
@RequestMapping("/userMedal")
@Api(tags = "用户勋章管理")
public class UserMedalController {

    @Resource
    private UserMedalService userMedalService;

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 分页查询勋章列表 - POST 方式
     * @param size 每页大小
     * @param current 当前页码
     * @param userId 用户 ID（可选）
     * @param userName 用户名（可选，模糊查询）
     * @param medalName 勋章名称（可选，模糊查询）
     * @return 分页结果
     */
    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询勋章列表（POST 方式）")
    public BaseResult<Page<UserMedalVO>> pageQuery(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "用户 ID（可选）", example = "1")
            @RequestParam(required = false) Integer userId,
            
            @ApiParam(value = "用户名（可选，模糊查询）", example = "张三")
            @RequestParam(required = false) String userName,
            
            @ApiParam(value = "勋章名称（可选，模糊查询）", example = "捐赠")
            @RequestParam(required = false) String medalName) {

        // 如果提供了用户名，则根据用户名查询用户 ID
        Integer finalUserId = userId;
        if (StringUtils.isNotBlank(userName)) {
            SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("name", userName));
            if (user != null) {
                finalUserId = user.getId();
            } else {
                // 用户名不存在，返回空结果
                Page<UserMedalVO> emptyPage = new Page<>(current, size, 0);
                return BaseResult.ok("查询成功", emptyPage);
            }
        }

        Page<UserMedalVO> page = userMedalService.pageQuery(current, size, finalUserId, medalName);
        return BaseResult.ok("查询成功", page);
    }

    /**
     * 根据 ID 查询勋章详情
     * @param id 勋章 ID
     * @return 勋章信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 ID 查询勋章详情")
    public BaseResult<UserMedalVO> getById(@PathVariable Integer id) {
        UserMedalVO userMedal = userMedalService.getById(id);
        if (userMedal == null) {
            return BaseResult.error("勋章不存在");
        }
        return BaseResult.ok("查询成功", userMedal);
    }

    /**
     * 新增勋章
     * @param userMedal 勋章信息
     * @return 是否成功
     */
    @PostMapping
    @ApiOperation("新增勋章")
    public BaseResult<Boolean> save(@RequestBody UserMedal userMedal) {
        // 参数校验
        if (userMedal.getUserId() == null) {
            return BaseResult.error("用户 ID 不能为空");
        }
        if (userMedal.getMedalType() == null) {
            return BaseResult.error("勋章类型不能为空");
        }
        if (userMedal.getRequiredCount() == null) {
            return BaseResult.error("所需数量不能为空");
        }
        
        // 检查用户是否已经拥有该勋章（根据 userId + medalType + requiredCount）
        UserMedal existingMedal = userMedalService.getByUserIdTypeAndCount(
            userMedal.getUserId(), 
            userMedal.getMedalType(), 
            userMedal.getRequiredCount()
        );
        
        if (existingMedal != null) {
            return BaseResult.error("您已经拥有该勋章【" + userMedal.getMedalName() + "】，无需重复解锁");
        }
        
        // 设置解锁时间为当前时间
        userMedal.setUnlockTime(new java.util.Date());
        
        boolean result = userMedalService.save(userMedal);
        if (result) {
            return BaseResult.ok("新增成功", result);
        }
        return BaseResult.error("新增失败");
    }

    /**
     * 修改勋章
     * @param userMedal 勋章信息
     * @return 是否成功
     */
    @PutMapping
    @ApiOperation("修改勋章")
    public BaseResult<Boolean> update(@RequestBody UserMedal userMedal) {
        boolean result = userMedalService.update(userMedal);
        if (result) {
            return BaseResult.ok("修改成功", result);
        }
        return BaseResult.error("修改失败");
    }

    /**
     * 删除勋章
     * @param id 勋章 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除勋章")
    public BaseResult<Boolean> delete(@PathVariable Integer id) {
        boolean result = userMedalService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", result);
        }
        return BaseResult.error("删除失败");
    }
}
