package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.UserMedal;
import com.book_drift.vo.UserMedalVO;

/**
 * <p>
 * 用户勋章服务接口
 * </p>
 */
public interface UserMedalService {

    /**
     * 分页查询勋章列表（支持用户 ID 条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户 ID（可选）
     * @return 分页结果
     */
    Page<UserMedalVO> pageQuery(Integer pageNum, Integer pageSize, Integer userId);

    /**
     * 分页查询勋章列表（支持用户 ID 和勋章名称条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户 ID（可选）
     * @param medalName 勋章名称（可选，模糊查询）
     * @return 分页结果
     */
    Page<UserMedalVO> pageQuery(Integer pageNum, Integer pageSize, Integer userId, String medalName);

    /**
     * 根据 ID 查询勋章
     * @param id 勋章 ID
     * @return 勋章信息
     */
    UserMedalVO getById(Integer id);

    /**
     * 保存勋章
     * @param userMedal 勋章信息
     * @return 是否成功
     */
    boolean save(UserMedal userMedal);

    /**
     * 更新勋章
     * @param userMedal 勋章信息
     * @return 是否成功
     */
    boolean update(UserMedal userMedal);

    /**
     * 删除勋章
     * @param id 勋章 ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 根据用户 ID 和勋章 ID 查询勋章
     * @param userId 用户 ID
     * @param medalId 勋章 ID
     * @return 勋章信息
     */
    UserMedal getByUserIdAndMedalId(Integer userId, Integer medalId);

    /**
     * 根据用户 ID、勋章类型和所需数量查询勋章
     * @param userId 用户 ID
     * @param medalType 勋章类型
     * @param requiredCount 所需数量
     * @return 勋章信息
     */
    UserMedal getByUserIdTypeAndCount(Integer userId, Integer medalType, Integer requiredCount);
}
