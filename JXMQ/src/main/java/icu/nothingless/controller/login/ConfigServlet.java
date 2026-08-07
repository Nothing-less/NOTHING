package icu.nothingless.controller.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.config.GlobalConfig;
import icu.nothingless.tools.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * URL模式: /api/config
 * 返回 JSON 格式的配置数据
 */
@WebServlet(name = "ConfigServlet", urlPatterns = "/api/config")
public class ConfigServlet extends HttpServlet {
    private static final String DEFAULT_PAGE = GlobalConfig.CONFIG_MAP.get("page.vanilla"); // 进入主页后默认显示的页面

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 构建配置
        Map<String, Object> config = new HashMap<>();
        config.put("contextPath", req.getContextPath());

        // 当前菜单
        String currentMenu = (String) req.getSession(false).getAttribute("MENU");

        config.put("currentMenu", currentMenu != null ? currentMenu : DEFAULT_PAGE);

        // 时间间隔配置
        Map<String, Integer> intervals = new HashMap<>();
        intervals.put("clock", 1000);
        intervals.put("sync", 30000);
        config.put("intervals", intervals);

        resp.getWriter().write(JsonUtil.toJson(RespEntity.success(config)));
    }
}