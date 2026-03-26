package com.book_drift.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.book_drift.domain.SysUser;
import com.book_drift.mapper.SysUserMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 活跃度重置定时任务
 */
@Component
public class ActivityResetTask {

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 每天凌晨0点重置日活跃度
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyActivity() {
        UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("daily_activity_score", 0);
        sysUserMapper.update(null, updateWrapper);
    }

    /**
     * 每周一凌晨0点重置周活跃度
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    public void resetWeeklyActivity() {
        UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("weekly_activity_score", 0);
        sysUserMapper.update(null, updateWrapper);
    }

    /**
     * 每月1号凌晨0点重置月活跃度
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void resetMonthlyActivity() {
        UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("monthly_activity_score", 0);
        sysUserMapper.update(null, updateWrapper);
    }
}
