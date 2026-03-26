package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book_drift.domain.Feedback;
import com.book_drift.vo.FeedbackVO;
import java.util.List;

public interface FeedbackService extends IService<Feedback> {
    List<FeedbackVO> getFeedbacksByUserId(Long userId);
    List<FeedbackVO> getAllFeedbacks();
    void updateFeedbackStatus(Long id, String status);
    void replyFeedback(Long id, String reply);
    
    Page<FeedbackVO> pageQueryByUserId(Long userId, Integer pageNum, Integer pageSize);
    Page<FeedbackVO> pageQueryAll(Integer pageNum, Integer pageSize);
}
