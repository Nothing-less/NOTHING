package icu.nothingless.tools;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
/*
 * 视图渲染工具类
 * data 存放: Session
 * JSP  存放: WEB-INF/jsp/
*/
public class ViewUtil {

    private static String viewPrefix = "/WEB-INF/jsp/";
    private static String viewSuffix = ".jsp";

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-/]+$");

    /**
     * 无数据渲染
     */
    public static void render(HttpServletRequest req, HttpServletResponse resp,
            String viewName) throws ServletException, IOException {
        render(req, resp, viewName, null);
    }

    public static void render(HttpServletRequest req, HttpServletResponse resp,
            String view_full_path, String view_name, Map<String, Object> data) throws ServletException, IOException {
        if(data != null) {
            HttpSession session = req.getSession();
            data.forEach(session::setAttribute);
        }
        if("".equals(view_full_path)){
            view_full_path = "/";
        }
        req.getRequestDispatcher(view_full_path+view_name+viewSuffix).forward(req, resp);
    }


    /**
     * 渲染视图
     * 
     * @param req      HttpServletRequest
     * @param resp     HttpServletResponse
     * @param viewName 视图名称
     * @param data     数据，存放到 Session 中
     */
    public static void render(HttpServletRequest req, HttpServletResponse resp,
            String viewName, Map<String, Object> data) throws ServletException, IOException {

        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException("View name cannot be empty");
        }

        // 防止路径遍历攻击
        String cleanName = sanitizeViewName(viewName);

        // 字符白名单校验
        if (!SAFE_NAME_PATTERN.matcher(cleanName).matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid view name format");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        if (data != null) {
            HttpSession session = req.getSession();
            data.forEach(session::setAttribute);
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
     * 视图名称清理
     */
    private static String sanitizeViewName(String viewName) {
        // 去除路径遍历关键字符
        String cleaned = viewName
                .replace("..", "") // 阻止../
                .replace("//", "/") // 规范化路径
                .replace("\\", "/") // 统一分隔符
                .trim();

        // 去除开头的/
        while (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }

        return cleaned;
    }
}
