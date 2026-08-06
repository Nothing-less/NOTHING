package icu.nothingless.filter;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/*"})
public class JwtAuthenticationFilter implements Filter {

    // 这些路径不需要 token 验证
    private static final String[] SKIP_PATHS = {
        "/login", "/static/", "/api/config", "/api/time"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String contextPath = request.getContextPath();
        String relativePath = request.getRequestURI().substring(contextPath.length());

        // 跳过静态资源和登录接口
        for (String skip : SKIP_PATHS) {
            if (relativePath.startsWith(skip)) {
                chain.doFilter(req, resp);
                return;
            }
        }

        // 从 Header 或 URL 参数提取 token
        String token = extractToken(request);

        if (token == null || !JwtUtil.validateToken(token)) {
            if (isAjax(request)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JsonUtil.toJson(RespEntity.badRequest("未登录或登录已过期")));
            } else {
                response.sendRedirect(contextPath + "/login");
            }
            return;
        }

        // 解析 token，构造 User 对象放到 request 属性中
        // ⚠️ 这里根据你的 User 类实际构造方法调整
        Claims claims = JwtUtil.parseToken(token);
        // User user = new User();
        request.setAttribute("CURRENT_USER", null); // ⚠️ 这里需要根据你的 User 类实际构造方法调整

        chain.doFilter(req, resp);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. 优先从 Header 读取（供 AJAX 使用）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 2. 其次从 URL 参数读取（供 iframe 首次加载使用）
        String paramToken = request.getParameter("token");
        if (paramToken != null && !paramToken.isEmpty()) {
            return paramToken;
        }
        return null;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}