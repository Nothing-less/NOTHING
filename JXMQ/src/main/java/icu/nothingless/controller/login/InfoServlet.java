package icu.nothingless.controller.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "InfoServlet", urlPatterns = "/api/user")
public class InfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("CURRENT_USER") : null;

        if (user == null) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.toJson(RespEntity.error("You didn't logon")));
            return;
        }

        // 只返回必要字段，过滤敏感信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.userId());
        userInfo.put("userAccount", user.userAccount());
        userInfo.put("roleId", user.roleId());
        // 不要返回密码！

        resp.getWriter().write(JsonUtil.toJson(RespEntity.success(user)));
    }
}
