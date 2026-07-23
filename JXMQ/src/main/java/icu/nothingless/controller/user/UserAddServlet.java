package icu.nothingless.controller.user;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 用户创建 API - 仅 Super Administrator 可调用
 * URL: /user/add
 */
@WebServlet("/user/add")
public class UserAddServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(UserAddServlet.class);
    private static final IUserService<User> userService = (IUserService<User>)ServiceFactory.getSingleton(IUserService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        
        // 1. 权限检查
        HttpSession session = req.getSession(false);
        if (session == null) {
            writeJson(resp, RespEntity.error("未登录"));
            return;
        }
        
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null || !"Super Administrator".equals(String.valueOf(currentUser.roleId()))) {
            writeJson(resp, RespEntity.error("无权访问"));
            return;
        }
        
        // 2. 参数获取与校验
        String account = req.getParameter("account");
        String password = req.getParameter("password");
        String nickname = req.getParameter("nickname");
        String roleIdStr = req.getParameter("roleId");
        
        if (account == null || account.trim().isEmpty()) {
            writeJson(resp, RespEntity.badRequest("账号不能为空"));
            return;
        }
        account = account.trim();

        int roleId = 0;
        try {
            if (roleIdStr != null) {
                int reqRoleId = Integer.parseInt(roleIdStr);
                if (reqRoleId != 0) {
                    logger.warn("User [{}] attempted to create user with roleId={}, forced to 0", 
                        currentUser.userId(), reqRoleId);
                }
            }
        } catch (NumberFormatException e) {
            // ignore, use default 0
        }
        
        // 4. 调用 Service 创建用户
        try {
            User newUser = User.builder()
                .userAccount(account)
                .userPasswd(password)
                .roleId(""+roleId)
                .nickname(nickname)
                .build();
            RespEntity<User> result = userService.doRegister(newUser);
            if(result.getCode() != 200) {
                writeJson(resp, RespEntity.error("创建用户失败: " + result.getMessage()));
                return;
            }else{
                writeJson(resp, RespEntity.success("用户创建成功"));
            }


            logger.info("User [{}] created new user [account={}] by Super Administrator", 
                currentUser.userId(), account);
                
        } catch (Exception e) {
            logger.error("Failed to create user [account={}]", account, e);
            writeJson(resp, RespEntity.error("创建用户失败: " + e.getMessage()));
        }
    }

    private void writeJson(HttpServletResponse resp, RespEntity<?> entity) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(entity));
    }
}