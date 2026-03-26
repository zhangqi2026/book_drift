package com.book_drift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.Feedback;
import com.book_drift.service.FeedbackService;
import com.book_drift.vo.BaseResult;
import com.book_drift.vo.FeedbackVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    // 提交反馈
    @PostMapping("/submit")
    public BaseResult submitFeedback(@RequestBody Feedback feedback) {
        feedback.setStatus("PENDING");
        feedback.setCreateTime(new Date());
        feedback.setUpdateTime(new Date());
        boolean saved = feedbackService.save(feedback);
        if (saved) {
            return BaseResult.ok("反馈提交成功");
        } else {
            return BaseResult.error("反馈提交失败");
        }
    }

    // 获取用户的反馈列表
    @PostMapping("/user/{userId}")
    public BaseResult getUserFeedbacks(@PathVariable Long userId) {
        List<FeedbackVO> feedbacks = feedbackService.getFeedbacksByUserId(userId);
        return BaseResult.ok("获取反馈列表成功", feedbacks);
    }

    // 获取所有反馈（管理员用）
    @PostMapping("/all")
    public BaseResult getAllFeedbacks() {
        List<FeedbackVO> feedbacks = feedbackService.getAllFeedbacks();
        return BaseResult.ok("获取反馈列表成功", feedbacks);
    }

    // 更新反馈状态
    @PostMapping("/updateStatus")
    public BaseResult updateFeedbackStatus(@RequestBody FeedbackStatusUpdateRequest request) {
        feedbackService.updateFeedbackStatus(request.getId(), request.getStatus());
        return BaseResult.ok("状态更新成功");
    }
    
    // 回复反馈
    @PostMapping("/reply")
    public BaseResult replyFeedback(@RequestBody FeedbackReplyRequest request) {
        feedbackService.replyFeedback(request.getId(), request.getReply());
        return BaseResult.ok("回复成功");
    }
    
    // 分页获取用户的反馈列表
    @PostMapping("/user/page/{userId}/{size}/{current}")
    public BaseResult<Page<FeedbackVO>> getUserFeedbacksPage(
            @PathVariable Long userId,
            @PathVariable int size,
            @PathVariable int current) {
        Page<FeedbackVO> page = feedbackService.pageQueryByUserId(userId, current, size);
        return BaseResult.ok("获取反馈列表成功", page);
    }

    // 分页获取所有反馈（管理员用）
    @PostMapping("/all/page/{size}/{current}")
    public BaseResult<Page<FeedbackVO>> getAllFeedbacksPage(
            @PathVariable int size,
            @PathVariable int current) {
        Page<FeedbackVO> page = feedbackService.pageQueryAll(current, size);
        return BaseResult.ok("获取反馈列表成功", page);
    }
    
    // 内部类，用于接收状态更新请求
    static class FeedbackStatusUpdateRequest {
        private Long id;
        private String status;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    // 内部类，用于接收回复请求
    static class FeedbackReplyRequest {
        private Long id;
        private String reply;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getReply() {
            return reply;
        }
        
        public void setReply(String reply) {
            this.reply = reply;
        }
    }
}
