package icu.nothingless.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;

import icu.nothingless.dto.UserDTO;
import icu.nothingless.service.impl.UserServiceImpl;
import icu.nothingless.service.interfaces.iUserService;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LoginServlet.class);
    private static final iUserService<UserDTO> userService = new UserServiceImpl();

    @Override
    protected void doGet( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String text = now.format(fmt);
        String token = "Testing token" + text;
        logger.error(token);
        UserDTO bean = new UserDTO();
        bean.setUserAccount(username);
        bean.setUserPasswd(password);
        bean.setLastLoginIpAddr("114.51.4.2");
        bean.setRoleId("Admin");
        bean.setUserId("7");
        bean.setUserStatus(false);
        var loginResult = userService.doLogin(bean);
        if(loginResult.isSuccess()){
            ViewUtil.render(req, resp, "example/index");
        }


        
    }

}

// Java: Clean Workspace