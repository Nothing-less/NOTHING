package icu.nothingless.controller.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.bean.MessageBean;
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

@WebServlet("/chat/poll")
public class ChatPollServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int POLL_TIMEOUT_SECONDS = 30;

    private final IMessageService<Message> messageService = new MessageServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(resp);
        Long userId = getCurrentUserId(req);
        if (userId == null) {
            writeJson(resp, RespEntity.error("会话已过期"));
            return;
        }

        BlockingQueue<Message> queue = ChatWebSocketServer.getWaitQueue(userId);
        List<Message> messages = new ArrayList<>();

        try {
            RespEntity<List<Message>> unreadResp = messageService.getUnreadMessages(userId);
            if (unreadResp.isError()) {
                writeRespEntity(resp, unreadResp);
                return;
            }

            if (unreadResp.getData() != null && !unreadResp.getData().isEmpty()) {
                messages.addAll(unreadResp.getData());
            }

            if (messages.isEmpty()) {
                Message message = queue.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (message != null) {
                    messages.add(message);
                    queue.drainTo(messages);
                }
            } else {
                queue.drainTo(messages);
            }

            writeJson(resp, RespEntity.success(messages));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeJson(resp, RespEntity.success(Collections.emptyList()));
        }
    }

    public static void pushMessage(Long userId, Message message) {
        ChatWebSocketServer.pushToWaitQueue(userId, message);
    }

    public static void pushMessage(Long userId, MessageBean messageBean) {
        ChatWebSocketServer.pushToWaitQueue(userId, messageBean);
    }

    private Long getCurrentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("CURRENT_USER_ID");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private void prepareResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
    }

    private void writeRespEntity(HttpServletResponse resp, RespEntity<?> respEntity) throws IOException {
        if (respEntity == null) {
            writeJson(resp, RespEntity.error("No response"));
            return;
        }
        if (respEntity.isError()) {
            writeJson(resp, RespEntity.error(respEntity.getMessage()));
            return;
        }
        writeJson(resp, RespEntity.success(respEntity.getData()));
    }

    private void writeJson(HttpServletResponse resp, Object body) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(body));
    }
}
