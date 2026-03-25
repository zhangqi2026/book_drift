package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.SysUser;
import com.book_drift.vo.SysUserVO;

/**
 * <p>
 * 系统用户服务接口
 * </p>
 */
public interface SysUserService {

    /**
     * 分页查询用户列表（支持姓名条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 姓名（可选，模糊查询）
     * @return 分页结果
     */
    Page<SysUserVO> pageQuery(Integer pageNum, Integer pageSize, String name);

    /**
     * 根据 ID 查询用户
     * @param id 用户 ID
     * @return 用户信息
     */
    SysUserVO getById(Integer id);

    /**
     * 保存用户
     * @param sysUser 用户信息
     * @return 是否成功
     */
    boolean save(SysUser sysUser);

    /**
     * 更新用户
     * @param sysUser 用户信息
     * @return 是否成功
     */
    boolean update(SysUser sysUser);

    /**
     * 删除用户
     * @param id 用户 ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 增加用户借书次数
     * @param userId 用户 ID
     */
    void increaseBorrowCount(Integer userId);

    /**
     * 检查并解锁勋章（根据借书次数）
     * @param userId 用户 ID
     * @param borrowCount 当前借书次数
     */
    void checkAndUnlockMedal(Integer userId, Integer borrowCount);

    /**
     * 判断是否为管理员
     * @param userId 用户 ID
     * @return true-是管理员 false-不是管理员
     */
    boolean isAdmin(Integer userId);

    /**
     * 验证用户权限
     * @param userId 用户 ID
     * @param requiredRole 所需角色
     * @throws RuntimeException 如果权限不足
     */
    void checkRole(Integer userId, Integer requiredRole);
}
