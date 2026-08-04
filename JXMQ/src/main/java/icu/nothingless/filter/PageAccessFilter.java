package icu.nothingless.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.pojo.dto.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 页面访问权限过滤器
 * 拦截 /page/* 请求，阻止越权访问
 */
@WebFilter(urlPatterns = "/page/*")
public class PageAccessFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(PageAccessFilter.class);
    
    // 权限配置：page_name -> 允许访问的角色列表
    private static final Map<String, List<String>> PAGE_ROLE_ACCESS = new HashMap<>();
    
    static {
        PAGE_ROLE_ACCESS.put("user_create", Arrays.asList(User.ROLE_SUPER_ADMIN));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        // 提取页面名称
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String pagePath = uri.substring(contextPath.length() + "/page/".length());
        
        // 去掉路径参数和查询参数
        int semicolonIndex = pagePath.indexOf(';');
        if (semicolonIndex != -1) pagePath = pagePath.substring(0, semicolonIndex);
        int queryIndex = pagePath.indexOf('?');
        if (queryIndex != -1) pagePath = pagePath.substring(0, queryIndex);
        
        String pageName = pagePath.toLowerCase().trim();
        
        // 检查权限
        if (!hasAccess(req, pageName)) {
            logger.warn("Access denied to page [{}]", pageName);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问此页面");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean hasAccess(HttpServletRequest req, String pageName) {
        // 页面没有权限配置，所有人可访问
        if (!PAGE_ROLE_ACCESS.containsKey(pageName)) {
            return true;
        }
        
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return false;
        
        Object roleId = currentUser.roleId();
        String userRole = roleId != null ? String.valueOf(roleId) : "";
        
        List<String> allowedRoles = PAGE_ROLE_ACCESS.get(pageName);
        return allowedRoles.contains(userRole);
    }
}