package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.constant.ActivityConstant;
import com.book_drift.domain.SysUser;
import com.book_drift.domain.UserActivityRecord;
import com.book_drift.mapper.SysUserMapper;
import com.book_drift.mapper.UserActivityRecordMapper;
import com.book_drift.service.UserActivityService;
import com.book_drift.vo.SysUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户活跃度服务实现类
 */
@Service
@Transactional
public class UserActivityServiceImpl extends ServiceImpl<UserActivityRecordMapper, UserActivityRecord> implements UserActivityService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public Integer addActivity(Integer userId, Integer activityType, Integer businessId) {
        int score = getScoreByType(activityType);
        addScore(userId, score);
        
        UserActivityRecord record = new UserActivityRecord();
        record.setUserId(userId);
        record.setActivityType(activityType);
        record.setScore(score);
        record.setBusinessId(businessId);
        record.setCreateTime(new Date());
        this.save(record);
        
        return score;
    }

    @Override
    public void addScore(Integer userId, Integer score) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        user.setActivityScore((user.getActivityScore() == null ? 0 : user.getActivityScore()) + score);
        user.setDailyActivityScore((user.getDailyActivityScore() == null ? 0 : user.getDailyActivityScore()) + score);
        user.setWeeklyActivityScore((user.getWeeklyActivityScore() == null ? 0 : user.getWeeklyActivityScore()) + score);
        user.setMonthlyActivityScore((user.getMonthlyActivityScore() == null ? 0 : user.getMonthlyActivityScore()) + score);
        
        sysUserMapper.updateById(user);
    }
    
    @Override
    public void subtractScore(Integer userId, Integer score) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        user.setActivityScore(Math.max(0, (user.getActivityScore() == null ? 0 : user.getActivityScore()) - score));
        user.setDailyActivityScore(Math.max(0, (user.getDailyActivityScore() == null ? 0 : user.getDailyActivityScore()) - score));
        user.setWeeklyActivityScore(Math.max(0, (user.getWeeklyActivityScore() == null ? 0 : user.getWeeklyActivityScore()) - score));
        user.setMonthlyActivityScore(Math.max(0, (user.getMonthlyActivityScore() == null ? 0 : user.getMonthlyActivityScore()) - score));
        
        sysUserMapper.updateById(user);
    }

    @Override
    public Page<SysUserVO> getRankList(String rankType, Integer pageNum, Integer pageSize) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("role", 1);
        
        switch (rankType) {
            case ActivityConstant.RANK_TYPE_DAILY:
                queryWrapper.orderByDesc("daily_activity_score");
                break;
            case ActivityConstant.RANK_TYPE_WEEKLY:
                queryWrapper.orderByDesc("weekly_activity_score");
                break;
            case ActivityConstant.RANK_TYPE_MONTHLY:
                queryWrapper.orderByDesc("monthly_activity_score");
                break;
            case ActivityConstant.RANK_TYPE_TOTAL:
            default:
                queryWrapper.orderByDesc("activity_score");
                break;
        }
        
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> userPage = sysUserMapper.selectPage(page, queryWrapper);
        
        List<SysUserVO> voList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        for (int i = 0; i < voList.size(); i++) {
            voList.get(i).setRank((pageNum - 1) * pageSize + i + 1);
        }
        
        Page<SysUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public SysUserVO getUserRank(Integer userId, String rankType) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        
        SysUserVO vo = convertToVO(user);
        
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("role", 1);
        
        Integer userScore = 0;
        String scoreField = "activity_score";
        
        if (ActivityConstant.RANK_TYPE_DAILY.equals(rankType)) {
            userScore = user.getDailyActivityScore();
            scoreField = "daily_activity_score";
        } else if (ActivityConstant.RANK_TYPE_WEEKLY.equals(rankType)) {
            userScore = user.getWeeklyActivityScore();
            scoreField = "weekly_activity_score";
        } else if (ActivityConstant.RANK_TYPE_MONTHLY.equals(rankType)) {
            userScore = user.getMonthlyActivityScore();
            scoreField = "monthly_activity_score";
        } else {
            userScore = user.getActivityScore();
            scoreField = "activity_score";
        }
        
        if (userScore == null) {
            userScore = 0;
        }
        
        Integer countResult = countUsersWithHigherScore(scoreField, userScore);
        vo.setRank(countResult + 1);
        
        return vo;
    }
    
    private Integer countUsersWithHigherScore(String scoreField, Integer score) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("role", 1);
        queryWrapper.apply(scoreField + " > {0}", score);
        return sysUserMapper.selectCount(queryWrapper);
    }

    private int getScoreByType(int activityType) {
        switch (activityType) {
            case ActivityConstant.ACTIVITY_TYPE_PUBLISH_BOOK:
                return ActivityConstant.SCORE_PUBLISH_BOOK;
            case ActivityConstant.ACTIVITY_TYPE_LEND_BOOK:
                return ActivityConstant.SCORE_LEND_BOOK;
            case ActivityConstant.ACTIVITY_TYPE_RETURN_CLAIM:
                return ActivityConstant.SCORE_RETURN_CLAIM;
            case ActivityConstant.ACTIVITY_TYPE_PUBLISH_NOTE:
                return ActivityConstant.SCORE_PUBLISH_NOTE;
            case ActivityConstant.ACTIVITY_TYPE_BE_LIKED:
                return ActivityConstant.SCORE_BE_LIKED;
            default:
                return 0;
        }
    }

    private SysUserVO convertToVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
