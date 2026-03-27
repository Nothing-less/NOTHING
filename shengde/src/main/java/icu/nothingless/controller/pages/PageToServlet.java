package icu.nothingless.controller.pages;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 页面渲染Servlet - 负责渲染JSP页面视图
 * URL模式: /page/dashboard, /page/orders 等
 */
@WebServlet("/page/*")
public class PageToServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PageToServlet.class);

    // 默认页面
    private static final String DEFAULT_PAGE = "dashboard";
    
    // 模板目录前缀
    private static final String TEMPLATE_PREFIX = "pages/";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String pageName = extractPageName(req.getPathInfo());
        
        // 安全校验：防止路径遍历和非法字符
        if (!isValidPageName(pageName)) {
            logger.warn("Invalid page name requested: {}", pageName);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        // 渲染视图
        try {
            String templatePath = TEMPLATE_PREFIX + pageName;
            logger.debug("Rendering page: {}", templatePath);
            ViewUtil.render(req, resp, templatePath);
        } catch (Exception e) {
            logger.error("Error rendering page: {} \n Error details: {}", pageName, e);
            ViewUtil.render(req, resp, "error_page",Map.of("respEntity",RespEntity.error("页面加载异常！")));
        }
    }
    
    /**
     * 从pathInfo提取页面名称
     */
    private String extractPageName(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return DEFAULT_PAGE;
        }
        
        // 移除开头的斜杠
        String pageName = pathInfo.substring(1);
        
        // 移除路径参数（如 ;jsessionid=xxx）
        int semicolonIndex = pageName.indexOf(';');
        if (semicolonIndex != -1) {
            pageName = pageName.substring(0, semicolonIndex);
        }
        
        // 移除查询参数（理论上不应该有，但以防万一）
        int queryIndex = pageName.indexOf('?');
        if (queryIndex != -1) {
            pageName = pageName.substring(0, queryIndex);
        }
        return pageName.toLowerCase().trim();
    }
    
    /**
     * 验证页面名称安全性
     */
    private boolean isValidPageName(String pageName) {
        if (pageName == null || pageName.isEmpty()) {
            return false;
        }
        
        // 只允许字母、数字、下划线、连字符
        return pageName.matches("^[a-zA-Z0-9_-]+$");
    }

}