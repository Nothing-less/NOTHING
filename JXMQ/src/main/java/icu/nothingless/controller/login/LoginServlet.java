package icu.nothingless.controller.login;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LoginServlet.class);

    protected static final IUserService<User> userService = (IUserService<User>) ServiceFactory
            .getSingleton(IUserService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.render(req, resp, "error_page", Map.of("respEntity", RespEntity.error("Request is not allowed")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        jakarta.servlet.http.HttpSession session = req.getSession(false);
        if(session != null){
            session.invalidate();
        }
        String username = req.getParameter("username");
        String password = req.getParameter("pwd_entrypted");
        // logger.debug(Fmt.of("User({})====PWD({})", username, password));

        User bean = User.builder()
                .userAccount(username).userPasswd(password).loginNow(getClientIP(req))
                .userStatus(true)
                .build();

        RespEntity<User> respEntity = userService.doLogin(bean);
        if (respEntity.isSuccess()) {
            session = req.getSession();
            User _user = (User) respEntity.getData();
            // 绑定用户和 Session，踢掉旧的
            icu.nothingless.listener.SessionListener.bindUserSession(_user.userId(), session);
            RedirectUtil.redirect(req, resp, "/home",
                    Map.of("CURRENT_USER", _user));
        } else {
            ViewUtil.render(req, resp, "error_page", Map.of("respEntity", respEntity));
        }

    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多级代理，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

}

// Java: Clean Workspace
