package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.constant.UserRoleConstant;
import com.book_drift.domain.SysUser;
import com.book_drift.service.SysUserService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.SysUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 系统用户控制器
 * </p>
 */
@RestController
@RequestMapping("/sysUser")
@Api(tags = "系统用户管理")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    /**
     * 分页查询用户列表 - POST 方式（类似老师的写法）
     * @param size 每页大小
     * @param current 当前页码
     * @param name 查询参数（name 等）
     * @return 分页结果
     */
    @PostMapping("/condition/{size}/{current}")
    @ApiOperation("分页查询用户列表（POST 方式）")
    public BaseResult<Page<SysUserVO>> condition(
            @ApiParam(value = "每页大小", required = true, example = "10")
            @PathVariable("size") int size,

            @ApiParam(value = "当前页码", required = true, example = "1")
            @PathVariable("current") int current,

            @ApiParam(value = "姓名（可选，模糊查询）", example = "张三")
            @RequestParam(required = false) String name) {

        Page<SysUserVO> page = sysUserService.pageQuery(current, size, name);
        return BaseResult.ok("查询成功", page);
    }


    /**
     * 根据 ID 查询用户详情
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 ID 查询用户详情")
    public BaseResult<SysUserVO> getById(@PathVariable Integer id) {
        SysUserVO user = sysUserService.getById(id);
        if (user == null) {
            return BaseResult.error("用户不存在");
        }
        return BaseResult.ok("查询成功", user);
    }

    /**
     * 新增用户
     * @param sysUser 用户信息
     * @return 是否成功
     */
    @PostMapping
    @ApiOperation("新增用户")
    public BaseResult<Boolean> save(@RequestBody SysUser sysUser) {
        // 如果没有设置角色，默认为普通用户
        if (sysUser.getRole() == null) {
            sysUser.setRole(UserRoleConstant.USER);
        }
        
        boolean result = sysUserService.save(sysUser);
        if (result) {
            return BaseResult.ok("新增成功", result);
        }
        return BaseResult.error("新增失败");
    }

    /**
     * 修改用户
     * @param sysUser 用户信息
     * @return 是否成功
     */
    @PutMapping
    @ApiOperation("修改用户")
    public BaseResult<Boolean> update(@RequestBody SysUser sysUser) {
        boolean result = sysUserService.update(sysUser);
        if (result) {
            return BaseResult.ok("修改成功", result);
        }
        return BaseResult.error("修改失败");
    }

    /**
     * 删除用户
     * @param id 用户 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public BaseResult<Boolean> delete(@PathVariable Integer id) {
        boolean result = sysUserService.delete(id);
        if (result) {
            return BaseResult.ok("删除成功", result);
        }
        return BaseResult.error("删除失败");
    }

    /**
     * 获取用户的借书次数和勋章信息
     * @param userId 用户 ID
     * @return 用户借书次数和勋章信息
     */
    @GetMapping("/borrowCount/{userId}")
    @ApiOperation("获取用户的借书次数和勋章信息")
    public BaseResult<SysUserVO> getBorrowCount(@PathVariable Integer userId) {
        SysUserVO user = sysUserService.getById(userId);
        if (user == null) {
            return BaseResult.error("用户不存在");
        }
        return BaseResult.ok("查询成功", user);
    }

    /**
     * 检查当前用户是否为管理员
     * @param userId 用户 ID
     * @return 是否为管理员
     */
    @GetMapping("/isAdmin/{userId}")
    @ApiOperation("检查是否为管理员")
    public BaseResult<Boolean> checkAdmin(@PathVariable Integer userId) {
        boolean isAdmin = sysUserService.isAdmin(userId);
        return BaseResult.ok(isAdmin ? "是管理员" : "不是管理员", isAdmin);
    }
}
