package icu.nothingless.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionDebugFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SessionDebugFilter.class);
    private final List<String> excludeUrls = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) {
        String excludes = filterConfig.getInitParameter("excludes");
        if (excludes != null) {
            for (String s : excludes.split(",")) {
                excludeUrls.add(s.trim());
            }
        }
        // logger.info("SessionDebugFilter initialized with excludes: \n");
        // excludeUrls.forEach(url -> logger.info("Excluded URL: {}", url));
    }

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        
        String contextPath = req.getContextPath();
        if (uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        if (uri.contains(".") || isExcluded(uri)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        // ================== 请求进来时 ==================
        printSession("【请求】", req);

        // 放行请求（进入 Servlet / JSP）
        chain.doFilter(request, response);

        // ================== 响应返回前 ==================
        printSession("【响应】", req);
    }

    private void printSession(String stage, HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n")
                .append(stage)
                .append(" ENTER ======================================\n")
                .append("URL: ").append(req.getRequestURL()).append("\n")
                .append("Thread: ").append(Thread.currentThread().getName()).append("\n");

        /* ========== Request Attributes ========== */
        sb.append("------ Request Attributes ------\n");
        Enumeration<String> reqNames = req.getAttributeNames();
        if (!reqNames.hasMoreElements()) {
            sb.append("(empty)\n");
        } else {
            while (reqNames.hasMoreElements()) {
                String name = reqNames.nextElement();
                Object value = req.getAttribute(name);
                sb.append("ReqAttr -> ")
                        .append(name)
                        .append(" = ")
                        .append(value)
                        .append("\n");
            }
        }
        sb.append("--------------------------------\n");

        /* ========== Session Attributes ========== */
        HttpSession session = req.getSession(false);
        sb.append("------ Session Attributes ------\n");
        if (session == null) {
            sb.append("Session is null\n");
        } else {
            sb.append("SessionId: ").append(session.getId()).append("\n");
            Enumeration<String> sessNames = session.getAttributeNames();
            if (!sessNames.hasMoreElements()) {
                sb.append("(empty)\n");
            } else {
                while (sessNames.hasMoreElements()) {
                    String name = sessNames.nextElement();
                    Object value = session.getAttribute(name);
                    sb.append("SessAttr -> ")
                            .append(name)
                            .append(" = ")
                            .append(value)
                            .append("\n");
                }
            }
        }

        sb.append(stage)
                .append(" END ======================================\n");

        logger.error(sb.toString());
    }

    private boolean isExcluded(String uri) {
        for (String pattern : excludeUrls) {
            if (pattern.endsWith("*")) {
                if (uri.startsWith(pattern.substring(0, pattern.length() - 1))) {
                    return true;
                }
            } else if (uri.equals(pattern)) {
                return true;
            }
        }
        return false;
    }
}
