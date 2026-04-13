package icu.nothingless.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import icu.nothingless.commons.R;
import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.service.impl.MessageServiceImpl;
import icu.nothingless.service.interfaces.IMessageService;
import icu.nothingless.tools.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/chat/poll")
public class ChatPollServlet extends HttpServlet {
    private IMessageService messageService = new MessageServiceImpl();
    // 存储等待队列(模拟WebSocket)
    private static final Map<Long, BlockingQueue<MessageBean>> waitQueues = new ConcurrentHashMap<>();
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        // 长轮询：等待新消息，最多30秒
        BlockingQueue<MessageBean> queue = waitQueues.computeIfAbsent(userId, k -> new LinkedBlockingQueue<>());
        
        try {
            // 先检查是否有离线消息
            List<MessageBean> offline = messageService.getUnreadMessages(userId);
            if (!offline.isEmpty()) {
                resp.getWriter().write(JsonUtil.toJson(R.success(offline)));
                return;
            }
            
            // 等待新消息，最多30秒
            MessageBean msg = queue.poll(30, TimeUnit.SECONDS);
            List<MessageBean> result = new ArrayList<>();
            if (msg != null) {
                result.add(msg);
                // 继续取队列中可能有的其他消息
                queue.drainTo(result);
            }
            
            resp.getWriter().write(JsonUtil.toJson(R.success(result)));
        } catch (InterruptedException e) {
            resp.getWriter().write(JsonUtil.toJson(R.success(Collections.emptyList())));
        }
    }
    
    // 供MessageService调用，当收到新消息时推送到等待队列
    public static void pushMessage(Long userId, MessageBean msg) {
        BlockingQueue<MessageBean> queue = waitQueues.get(userId);
        if (queue != null) {
            queue.offer(msg);
        }
    }
}