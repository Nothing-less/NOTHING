package icu.nothingless.controller.pages;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 新增用户
 * URL: /page/user_create
 */
@WebServlet("/page/user_add")
public class UserAddPageServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(UserAddPageServlet.class);
    private static final String PAGE_NAME = "user_create"; // 对应的JSP页面名

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }
        
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }
        
        // 2. 检查权限
        Object roleId = currentUser.roleId();
        if (roleId == null || !User.ROLE_SUPER_ADMIN.equals(String.valueOf(roleId))) {
            logger.warn("User [{}] attempted to access user_add without permission", currentUser.userId());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问此页面");
            return;
        }
        
        // 3. 渲染页面
        try {
            ViewUtil.render(req, resp, PAGE_NAME);
        } catch (Exception e) {
            logger.error("Error rendering user_add page", e);
            ViewUtil.render(req, resp, "error_page", 
                Map.of("respEntity", RespEntity.error("页面加载异常！")));
        }
    }
}