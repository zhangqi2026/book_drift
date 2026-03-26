package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.vo.SysUserVO;

/**
 * 用户活跃度服务接口
 */
public interface UserActivityService {
    
    /**
     * 增加用户活跃度
     * @param userId 用户 ID
     * @param activityType 活动类型
     * @param businessId 相关业务 ID
     * @return 增加的积分
     */
    Integer addActivity(Integer userId, Integer activityType, Integer businessId);
    
    /**
     * 增加用户活跃度（不记录活动记录）
     * @param userId 用户 ID
     * @param score 增加的分数
     */
    void addScore(Integer userId, Integer score);
    
    /**
     * 减少用户活跃度（不记录活动记录）
     * @param userId 用户 ID
     * @param score 减少的分数
     */
    void subtractScore(Integer userId, Integer score);
    
    /**
     * 获取排行榜
     * @param rankType 排行榜类型：daily-日榜 weekly-周榜 monthly-月榜 total-总榜
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<SysUserVO> getRankList(String rankType, Integer pageNum, Integer pageSize);
    
    /**
     * 获取用户的排名
     * @param userId 用户 ID
     * @param rankType 排行榜类型
     * @return 排名信息（包含用户信息和排名）
     */
    SysUserVO getUserRank(Integer userId, String rankType);
}
