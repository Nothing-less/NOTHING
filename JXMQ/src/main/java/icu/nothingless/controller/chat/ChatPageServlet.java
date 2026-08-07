package icu.nothingless.controller.chat;

import java.io.IOException;
import java.util.Map;

import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/chat/page")
public class ChatPageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }
        Object userIdObj = session.getAttribute("CURRENT_USER_ID");
        if (userIdObj == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }

        String friendId = req.getParameter("friendId");
        String nickname = req.getParameter("nickname");

        if (friendId == null || friendId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing friend ID param");
            return;
        }
        // 转发到 chat_window.jsp
        ViewUtil.render(req, resp, "pages/chat_window", Map.of("friendId", friendId, "nickname", nickname));
    }
}