package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.Feedback;
import com.book_drift.domain.SysUser;
import com.book_drift.mapper.FeedbackMapper;
import com.book_drift.mapper.SysUserMapper;
import com.book_drift.service.FeedbackService;
import com.book_drift.vo.FeedbackVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Resource
    private FeedbackMapper feedbackMapper;
    
    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public List<FeedbackVO> getFeedbacksByUserId(Long userId) {
        List<Feedback> feedbacks = feedbackMapper.selectList(
            new QueryWrapper<Feedback>()
                .eq("user_id", userId)
                .orderByDesc("create_time")
        );
        return feedbacks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<FeedbackVO> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackMapper.selectList(
            new QueryWrapper<Feedback>()
                .orderByDesc("create_time")
        );
        return feedbacks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void updateFeedbackStatus(Long id, String status) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback != null) {
            feedback.setStatus(status);
            feedback.setUpdateTime(new Date());
            feedbackMapper.updateById(feedback);
        }
    }

    @Override
    public void replyFeedback(Long id, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback != null) {
            feedback.setReply(reply);
            feedback.setReplyTime(new Date());
            feedback.setUpdateTime(new Date());
            feedbackMapper.updateById(feedback);
        }
    }

    @Override
    public Page<FeedbackVO> pageQueryByUserId(Long userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        Page<Feedback> feedbackPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        List<FeedbackVO> voList = feedbackPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<FeedbackVO> voPage = new Page<>(feedbackPage.getCurrent(), feedbackPage.getSize(), feedbackPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public Page<FeedbackVO> pageQueryAll(Integer pageNum, Integer pageSize) {
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        Page<Feedback> feedbackPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        List<FeedbackVO> voList = feedbackPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<FeedbackVO> voPage = new Page<>(feedbackPage.getCurrent(), feedbackPage.getSize(), feedbackPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    private FeedbackVO convertToVO(Feedback feedback) {
        FeedbackVO vo = new FeedbackVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setContent(feedback.getContent());
        vo.setReply(feedback.getReply());
        vo.setStatus(feedback.getStatus());
        vo.setCreateTime(feedback.getCreateTime());
        vo.setUpdateTime(feedback.getUpdateTime());
        vo.setReplyTime(feedback.getReplyTime());
        
        // 获取用户名
        SysUser user = sysUserMapper.selectById(feedback.getUserId());
        if (user != null) {
            vo.setUsername(user.getName());
        }
        
        return vo;
    }
}
