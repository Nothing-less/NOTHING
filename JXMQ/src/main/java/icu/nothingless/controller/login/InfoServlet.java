package icu.nothingless.controller.login;

import java.io.IOException;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.RedirectUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * URL模式: /api/user
 * 返回 JSON 格式的用户信息
 */
@WebServlet(name = "InfoServlet", urlPatterns = "/api/user")
public class InfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("CURRENT_USER") : null;
        if(user == null){
            user = RedirectUtil.getFlash(req, "CURRENT_USER") instanceof User u ? u : null;
        }
        if (user == null) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.toJson(RespEntity.error("You didn't logon")));
            return;
        }
        resp.getWriter().write(JsonUtil.toJson(RespEntity.success(user)));
    }
}
