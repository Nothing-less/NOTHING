package icu.nothingless.controller.config;

import java.io.IOException;
import java.util.HashMap; // 或用你项目现有的JSON工具
import java.util.Map;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.tools.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 如果没有用注解，在 web.xml 中配置
@WebServlet(name = "ConfigServlet", urlPatterns = "/api/config")
public class ConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        // 检查登录
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("CURRENT_USER") == null) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.toJson(RespEntity.error("You're not authorizated")));
            return;
        }

        // 构建配置
        Map<String, Object> config = new HashMap<>();
        config.put("contextPath", req.getContextPath());

        // 当前菜单
        String currentMenu = (String) session.getAttribute("MENU");
        config.put("currentMenu", currentMenu != null ? currentMenu : "dashboard");

        // 时间间隔配置
        Map<String, Integer> intervals = new HashMap<>();
        intervals.put("clock", 1000);
        intervals.put("sync", 30000);
        config.put("intervals", intervals);

        resp.getWriter().write(JsonUtil.toJson(RespEntity.success(config)));
    }
}