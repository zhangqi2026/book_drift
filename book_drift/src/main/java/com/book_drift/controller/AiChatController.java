package com.book_drift.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.book_drift.config.AiConfig;
import com.book_drift.domain.AiChat;
import com.book_drift.service.AiChatService;
import com.book_drift.vo.BaseResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    @Resource
    private AiConfig aiConfig;

    @PostMapping(value = "/chat/stream")
    public void chatStream(@RequestBody ChatStreamRequest request, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try {
            PrintWriter writer = response.getWriter();
            callQwenAPIStream(request.getQuestion(), request.getHistory(), writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callQwenAPIStream(String question, List<MessageItem> history, PrintWriter writer) throws Exception {
        String fullApiUrl = aiConfig.getApiUrl();
        if (!fullApiUrl.endsWith("/chat/completions")) {
            if (fullApiUrl.endsWith("/")) {
                fullApiUrl += "chat/completions";
            } else {
                fullApiUrl += "/chat/completions";
            }
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("stream", true);
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位专业的书籍知识助手。你的任务是：\n" +
            "1. 为用户推荐各类优质书籍\n" +
            "2. 解答关于书籍的问题\n" +
            "3. 分享阅读建议和心得\n" +
            "4. 根据用户的兴趣推荐合适的书籍\n" +
            "\n" +
            "请用友好、热情的语气回答，并提供详细的推荐理由。\n" +
            "【重要排版要求】：\n" +
            "1. 每个段落之间用换行符（\\n）分隔，不要挤在一起\n" +
            "2. 列表内容每条单独占一行，开头用数字或符号标记\n" +
            "3. 重要内容单独成段\n" +
            "4. 不要用圆点（•）代替换行，必须使用真正的换行符（\\n）\n" +
            "5. 保持排版清晰、易读，段落分明\n" +
            "6. 【关键】段落之间最多用两个换行符（\\n\\n），不要超过两个，避免间距过大");
        messages.add(systemMessage);
        
        if (history != null) {
            for (MessageItem item : history) {
                JSONObject msg = new JSONObject();
                msg.put("role", item.getRole());
                msg.put("content", item.getContent());
                messages.add(msg);
            }
        }
        
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        URL url = new URL(fullApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + aiConfig.getApiKey());
        connection.setDoOutput(true);

        connection.getOutputStream().write(requestBody.toJSONString().getBytes("UTF-8"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        StringBuilder fullResponse = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6);
                if ("[DONE]".equals(data)) {
                    break;
                }
                
                try {
                    JSONObject json = JSON.parseObject(data);
                    JSONArray choices = json.getJSONArray("choices");
                    if (choices != null && choices.size() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject delta = choice.getJSONObject("delta");
                        if (delta != null && delta.containsKey("content")) {
                            String content = delta.getString("content");
                            if (content != null) {
                                fullResponse.append(content);
                                writer.print("data: " + content + "\n\n");
                                writer.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        
        reader.close();
        connection.disconnect();
        
        writer.print("data: [DONE]\n\n");
        writer.flush();
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

    static class MessageItem {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    static class ChatStreamRequest {
        private String question;
        private List<MessageItem> history;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<MessageItem> getHistory() {
            return history;
        }

        public void setHistory(List<MessageItem> history) {
            this.history = history;
        }
    }
}
