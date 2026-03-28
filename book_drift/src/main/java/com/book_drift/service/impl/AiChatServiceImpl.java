package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.AiChat;
import com.book_drift.mapper.AiChatMapper;
import com.book_drift.service.AiChatService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiChatServiceImpl extends ServiceImpl<AiChatMapper, AiChat> implements AiChatService {

    @Override
    public List<AiChat> getSessionMessages(Long sessionId) {
        QueryWrapper<AiChat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                   .orderByAsc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getUserSessions(Long userId) {
        QueryWrapper<AiChat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        
        List<AiChat> allMessages = this.list(queryWrapper);
        
        Map<Long, List<AiChat>> sessionMap = allMessages.stream()
                .collect(Collectors.groupingBy(AiChat::getSessionId));
        
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (Map.Entry<Long, List<AiChat>> entry : sessionMap.entrySet()) {
            List<AiChat> messages = entry.getValue();
            AiChat firstMessage = messages.get(0);
            
            Map<String, Object> session = new HashMap<>();
            session.put("sessionId", entry.getKey());
            session.put("title", firstMessage.getContent().length() > 30 
                    ? firstMessage.getContent().substring(0, 30) + "..." 
                    : firstMessage.getContent());
            session.put("createTime", firstMessage.getCreateTime());
            
            sessions.add(session);
        }
        
        sessions.sort((a, b) -> ((Date) b.get("createTime")).compareTo((Date) a.get("createTime")));
        
        return sessions;
    }

    @Override
    public void clearUserSessions(Long userId) {
        QueryWrapper<AiChat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        this.remove(queryWrapper);
    }
}
