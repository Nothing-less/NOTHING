package icu.nothingless.controller;

import java.io.IOException;
import java.util.List;

import icu.nothingless.commons.R;
import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.Message;
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
    private static final long serialVersionUID = 1L;
    private final IMessageService<Message> messageService = new MessageServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(resp);
        Long userId = getCurrentUserId(req);
        if (userId == null) {
            writeJson(resp, R.error("未登录或会话已过期"));
            return;
        }

        String path = req.getPathInfo();
        if (path == null) {
            writeJson(resp, R.error("无效请求路径"));
            return;
        }

        if ("/history".equals(path)) {
            handleHistory(req, resp, userId);
        } else if ("/unread".equals(path)) {
            handleUnread(resp, userId);
        } else {
            writeJson(resp, R.error("无效请求路径"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(resp);
        Long userId = getCurrentUserId(req);
        if (userId == null) {
            writeJson(resp, R.error("未登录或会话已过期"));
            return;
        }

        String path = req.getPathInfo();
        if (path == null) {
            writeJson(resp, R.error("无效请求路径"));
            return;
        }

        if ("/send".equals(path)) {
            handleSend(req, resp, userId);
        } else if ("/read".equals(path)) {
            handleMarkAsRead(req, resp, userId);
        } else if ("/recall".equals(path)) {
            handleRecall(req, resp, userId);
        } else {
            writeJson(resp, R.error("无效请求路径"));
        }
    }

    private void handleHistory(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        Long lastMsgId = parseLongParameter(req, "lastMsgId");
        int limit = parseIntParameter(req, "limit", 20);

        if (friendId == null) {
            writeJson(resp, R.error("缺少 friendId 参数"));
            return;
        }

        RespEntity<List<Message>> respEntity = messageService.getChatHistory(userId, friendId, lastMsgId, limit);
        writeRespEntity(resp, respEntity);
    }

    private void handleUnread(HttpServletResponse resp, Long userId) throws IOException {
        RespEntity<List<Message>> respEntity = messageService.getUnreadMessages(userId);
        writeRespEntity(resp, respEntity);
    }

    private void handleSend(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long receiverId = parseLongParameter(req, "receiverId");
        String content = req.getParameter("content");
        Integer msgType = parseIntegerParameter(req, "msgType");

        if (receiverId == null) {
            writeJson(resp, R.error("缺少 receiverId 参数"));
            return;
        }
        if (content == null || content.isBlank()) {
            writeJson(resp, R.error("消息内容不能为空"));
            return;
        }

        RespEntity<Message> respEntity = messageService.sendMessage(userId, receiverId, content, msgType);
        writeRespEntity(resp, respEntity);
    }

    private void handleMarkAsRead(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long friendId = parseLongParameter(req, "friendId");
        if (friendId == null) {
            writeJson(resp, R.error("缺少 friendId 参数"));
            return;
        }
        RespEntity<Void> respEntity = messageService.markAsRead(userId, friendId);
        writeRespEntity(resp, respEntity);
    }

    private void handleRecall(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        Long msgId = parseLongParameter(req, "msgId");
        if (msgId == null) {
            writeJson(resp, R.error("缺少 msgId 参数"));
            return;
        }
        RespEntity<Void> respEntity = messageService.recallMessage(msgId, userId);
        writeRespEntity(resp, respEntity);
    }

    private Long getCurrentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Long parseLongParameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerParameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntParameter(HttpServletRequest req, String name, int defaultValue) {
        Integer value = parseIntegerParameter(req, name);
        return value == null ? defaultValue : value;
    }

    private void prepareResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
    }

    private void writeRespEntity(HttpServletResponse resp, RespEntity<?> respEntity) throws IOException {
        if (respEntity == null) {
            writeJson(resp, R.error("服务器返回空响应"));
            return;
        }
        if (respEntity.isError()) {
            writeJson(resp, R.error(respEntity.getMessage()));
            return;
        }
        writeJson(resp, R.success(respEntity.getData()));
    }

    private void writeJson(HttpServletResponse resp, Object body) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(body));
    }
}
