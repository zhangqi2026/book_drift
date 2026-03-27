package com.book_drift.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.book_drift.domain.AiChat;

import java.util.List;
import java.util.Map;

public interface AiChatService extends IService<AiChat> {
    
    List<AiChat> getSessionMessages(Long sessionId);
    
    List<Map<String, Object>> getUserSessions(Long userId);
    
    void clearUserSessions(Long userId);
}
