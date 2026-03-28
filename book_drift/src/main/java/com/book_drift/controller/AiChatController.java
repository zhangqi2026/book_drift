package com.book_drift.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.book_drift.config.AiConfig;
import com.book_drift.domain.AiChat;
import com.book_drift.service.AiChatService;
import com.book_drift.vo.BaseResult;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    @Resource
    private AiConfig aiConfig;

    @Resource
    private RestTemplate restTemplate;

    @PostMapping("/chat")
    public BaseResult<String> chat(@RequestBody ChatRequest request) {
        String question = request.getQuestion();

        try {
            String aiResponse = callQwenAPI(question);
            return BaseResult.ok("获取回答成功", aiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResult.error("API调用失败：" + e.getMessage());
        }
    }

    private String callQwenAPI(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiConfig.getModel());
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位专业的书籍知识助手。你的任务是：\n" +
            "1. 为用户推荐各类优质书籍\n" +
            "2. 解答关于书籍的问题\n" +
            "3. 分享阅读建议和心得\n" +
            "4. 根据用户的兴趣推荐合适的书籍\n" +
            "\n" +
            "请用友好、热情的语气回答，并提供详细的推荐理由。");
        messages.add(systemMessage);
        
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

        String fullApiUrl = aiConfig.getApiUrl();
        if (!fullApiUrl.endsWith("/chat/completions")) {
            if (fullApiUrl.endsWith("/")) {
                fullApiUrl += "chat/completions";
            } else {
                fullApiUrl += "/chat/completions";
            }
        }
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            fullApiUrl, 
            entity, 
            String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject responseBody = JSON.parseObject(response.getBody());
            JSONArray choices = responseBody.getJSONArray("choices");
            if (choices != null && choices.size() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject messageObj = choice.getJSONObject("message");
                if (messageObj != null) {
                    return messageObj.getString("content");
                }
            }
        }

        throw new RuntimeException("API调用失败");
    }

    @PostMapping("/save")
    public BaseResult saveChat(@RequestBody SaveChatRequest request) {
        try {
            System.out.println("开始保存对话，参数：" + JSON.toJSONString(request));
            
            if (request.getUserId() == null) {
                return BaseResult.error("用户ID不能为空");
            }
            if (request.getSessionId() == null) {
                return BaseResult.error("会话ID不能为空");
            }
            if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
                return BaseResult.error("问题不能为空");
            }
            if (request.getAnswer() == null || request.getAnswer().isEmpty()) {
                return BaseResult.error("回答不能为空");
            }
            
            Date now = new Date();

            AiChat userChat = new AiChat();
            userChat.setUserId(request.getUserId());
            userChat.setSessionId(request.getSessionId());
            userChat.setRole("user");
            userChat.setContent(request.getQuestion());
            userChat.setCreateTime(now);
            System.out.println("保存用户消息：" + JSON.toJSONString(userChat));
            boolean userSaved = aiChatService.save(userChat);
            System.out.println("用户消息保存结果：" + userSaved);

            AiChat aiChat = new AiChat();
            aiChat.setUserId(request.getUserId());
            aiChat.setSessionId(request.getSessionId());
            aiChat.setRole("assistant");
            aiChat.setContent(request.getAnswer());
            aiChat.setCreateTime(now);
            System.out.println("保存AI消息：" + JSON.toJSONString(aiChat));
            boolean aiSaved = aiChatService.save(aiChat);
            System.out.println("AI消息保存结果：" + aiSaved);

            System.out.println("对话保存成功");
            return BaseResult.ok("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("保存对话失败：" + e.getMessage());
            return BaseResult.error("保存失败：" + e.getMessage());
        }
    }

    private String filterEmoji(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c <= 0xFFFF) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @PostMapping("/history/{userId}")
    public BaseResult<List<Map<String, Object>>> getHistory(@PathVariable Long userId) {
        List<Map<String, Object>> sessions = aiChatService.getUserSessions(userId);
        return BaseResult.ok("获取历史记录成功", sessions);
    }

    @PostMapping("/session/{sessionId}")
    public BaseResult<List<Map<String, Object>>> getSession(@PathVariable Long sessionId) {
        List<AiChat> messages = aiChatService.getSessionMessages(sessionId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiChat chat : messages) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", chat.getRole());
            msg.put("content", chat.getContent());
            msg.put("time", chat.getCreateTime());
            result.add(msg);
        }
        
        return BaseResult.ok("获取会话成功", result);
    }

    @PostMapping("/clear/{userId}")
    public BaseResult clearHistory(@PathVariable Long userId) {
        aiChatService.clearUserSessions(userId);
        return BaseResult.ok("清空成功");
    }

    @PostMapping("/delete/{sessionId}")
    public BaseResult deleteSession(@PathVariable Long sessionId) {
        try {
            aiChatService.remove(new QueryWrapper<AiChat>().eq("session_id", sessionId));
            return BaseResult.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResult.error("删除失败：" + e.getMessage());
        }
    }

    private String generateAIResponse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "请输入您的问题。";
        }

        if (question.contains("书籍") || question.contains("书")) {
            return "关于书籍的问题，您可以在借阅大厅查看所有可借阅的书籍，或者在我的记录中查看您的借阅历史。如果有其他问题，欢迎继续提问！";
        }
        
        if (question.contains("借阅") || question.contains("借")) {
            return "借阅书籍很简单！您可以在借阅大厅选择喜欢的书籍，点击认领按钮即可。每本书可以借阅30天，请记得按时归还哦！";
        }
        
        if (question.contains("归还") || question.contains("还")) {
            return "归还书籍时，请在我的记录中找到对应书籍，点击归还按钮即可。归还后书籍会重新回到借阅大厅供其他人借阅。";
        }
        
        if (question.contains("反馈") || question.contains("建议")) {
            return "您有任何意见或建议都可以在反馈建议页面提交，我们会认真对待每一条反馈！";
        }
        
        if (question.contains("勋章") || question.contains("成就")) {
            return "通过捐赠书籍、借阅书籍和分享笔记可以获得不同的勋章，您可以在漂流勋章页面查看所有勋章！";
        }

        return "感谢您的提问！这是一个模拟的AI回答。在实际使用时，这里会接入真实的AI API来提供智能回答。您可以尝试问我关于书籍借阅、归还、反馈等相关问题哦！";
    }

    static class ChatRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    static class SaveChatRequest {
        private Long userId;
        private Long sessionId;
        private String question;
        private String answer;
        private String title;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
