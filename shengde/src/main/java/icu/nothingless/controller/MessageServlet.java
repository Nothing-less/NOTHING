package icu.nothingless.controller;

import java.io.IOException;
import java.util.List;

import icu.nothingless.commons.ResultEntity;
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

@WebServlet("/message/*")
public class MessageServlet extends HttpServlet {
    private IMessageService messageService = new MessageServiceImpl();
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        String path = req.getPathInfo();
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if ("/history".equals(path)) {
            // 获取聊天记录
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            String lastId = req.getParameter("lastMsgId");
            Long lastMsgId = lastId != null ? Long.parseLong(lastId) : null;
            int limit = 20;
            
            List<MessageBean> list = messageService.getChatHistory(userId, friendId, lastMsgId, limit);
            resp.getWriter().write(JsonUtil.toJson(ResultEntity.success(list)));
            
        } else if ("/unread".equals(path)) {
            // 获取所有未读消息
            List<MessageBean> list = messageService.getUnreadMessages(userId);
            resp.getWriter().write(JsonUtil.toJson(ResultEntity.success(list)));
        }
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        String path = req.getPathInfo();
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if ("/send".equals(path)) {
            // 发送消息
            Long receiverId = Long.parseLong(req.getParameter("receiverId"));
            String content = req.getParameter("content");
            Integer msgType = Integer.parseInt(req.getParameter("msgType"));
            
            MessageBean msg = messageService.sendMessage(userId, receiverId, content, msgType);
            resp.getWriter().write(JsonUtil.toJson(msg != null ? ResultEntity.success(msg) : ResultEntity.error("发送失败")));
            
        } else if ("/read".equals(path)) {
            // 标记已读
            Long friendId = Long.parseLong(req.getParameter("friendId"));
            messageService.markAsRead(userId, friendId);
            resp.getWriter().write(JsonUtil.toJson(ResultEntity.success(null)));
            
        } else if ("/recall".equals(path)) {
            // 撤回消息
            Long msgId = Long.parseLong(req.getParameter("msgId"));
            boolean success = messageService.recallMessage(msgId, userId);
            resp.getWriter().write(JsonUtil.toJson(success ? ResultEntity.success(null) : ResultEntity.error("撤回失败，超过2分钟")));
        }
    }
}