package icu.nothingless.controller.user;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 用户个人信息更新 API
 */
@WebServlet("/user/update")
public class UserUpdateServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(UserUpdateServlet.class);
    private final IUserService<User> userService = ServiceFactory.getSingleton(IUserService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = req.getSession(false);
        if (session == null) {
            writeJson(resp, RespEntity.unauthorized("未登录"));
            return;
        }
        
        User currentUser = (User) icu.nothingless.tools.RedirectUtil.getFlash(req, "CURRENT_USER");
        if (currentUser == null) {
            writeJson(resp, RespEntity.unauthorized("未登录"));
            return;
        }
        
        String nickname = req.getParameter("nickname");
        String userInfos = req.getParameter("userInfos");
        
        if (nickname == null || nickname.trim().isEmpty()) {
            writeJson(resp, RespEntity.badRequest("昵称不能为空"));
            return;
        }
        nickname = nickname.trim();
        if (nickname.length() > 50) {
            writeJson(resp, RespEntity.badRequest("昵称长度不能超过50个字符"));
            return;
        }
        if (userInfos != null && userInfos.length() > 500) {
            writeJson(resp, RespEntity.badRequest("个人简介长度不能超过500个字符"));
            return;
        }
        
        try {
            User updateUser = User.forUpdate(
                currentUser.userId(),
                nickname,
                userInfos
            );
            var result = userService.doUpdate(updateUser);
            
            if (result.isSuccess() && result.getData() != null) {
                session.setAttribute(RedirectUtil.PREFIX + "CURRENT_USER", result.getData());
            }
            
            writeJson(resp, result);
            
        } catch (Exception e) {
            logger.error("Update profile failed for user [{}]", currentUser.userId(), e);
            writeJson(resp, RespEntity.error("更新失败: " + e.getMessage()));
        }
    }
    
    private void writeJson(HttpServletResponse resp, RespEntity<?> entity) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(entity));
    }
}
