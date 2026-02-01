package icu.nothingless.tools;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ViewUtil {

    private static String viewPrefix = "/WEB-INF/jsp/";
    private static String viewSuffix = ".jsp";

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-/]+$");

    /**
     * 便捷方法：无数据渲染
     */
    public static void render(HttpServletRequest req, HttpServletResponse resp,
            String viewName) throws ServletException, IOException {
        render(req, resp, viewName, null);
    }

    public static void render(HttpServletRequest req, HttpServletResponse resp,
            String viewName, Map<String, Object> data) throws ServletException, IOException {

        // 严格参数校验
        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException("View name cannot be empty");
        }

        // 防止路径遍历攻击（多层防护）
        String cleanName = sanitizeViewName(viewName);

        // 字符白名单校验（防止特殊字符注入）
        if (!SAFE_NAME_PATTERN.matcher(cleanName).matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid view name format");
            return;
        }

        // 强制设置UTF-8编码（防止中文乱码）
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        if (data != null) {
            data.forEach(req::setAttribute);
        }

        // 构建完整路径
        String dispatchPath = viewPrefix + cleanName + viewSuffix;
        req.getRequestDispatcher(dispatchPath).forward(req, resp);
    }

    public static void init(ServletContext context) {
        String prefix = context.getInitParameter("view.prefix");
        String suffix = context.getInitParameter("view.suffix");

        // 确保前缀以/开头，以/结尾
        if (prefix != null)
            viewPrefix = prefix;
        if (suffix != null)
            viewSuffix = suffix;

        // 确保前缀以/开头，以/结尾
        if (!viewPrefix.startsWith("/"))
            viewPrefix = "/" + viewPrefix;
        if (!viewPrefix.endsWith("/"))
            viewPrefix = viewPrefix + "/";
    }

    /**
     * 视图名称清理（安全核心）
     */
    private static String sanitizeViewName(String viewName) {
        // 去除路径遍历关键字符
        String cleaned = viewName
                .replace("..", "") // 阻止../
                .replace("//", "/") // 规范化路径
                .replace("\\", "/") // 统一分隔符
                .trim();

        // 去除开头的/，防止绝对路径绕过
        while (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }

        return cleaned;
    }
}
