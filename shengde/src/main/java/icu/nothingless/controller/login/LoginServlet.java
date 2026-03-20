package icu.nothingless.controller.login;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.dto.UserDTO;
import icu.nothingless.service.interfaces.iUserService;
import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {

    private static final String MENU = "example_tables";

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LoginServlet.class);
    
    private static final iUserService<UserDTO> userService = (iUserService<UserDTO>)ServiceFactory.getSingleton(iUserService.class);

    @Override
    protected void doGet( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException{

        String username = req.getParameter("username");
        logger.warn("TEST-TEST:::{}",req.getParameter("password"));
        String password = req.getParameter("pwd_entrypted");
        logger.error("UserName::{}----PassWord::{}",username,password);
        UserDTO bean = new UserDTO();
        bean.setUserAccount(username);
        bean.setUserPasswd(password);
        bean.setLastLoginIpAddr("114.51.4.2");
        bean.setRoleId("Admin");
        bean.setUserId("7");
        bean.setUserStatus(true);
        RespEntity<UserDTO> respEntity = userService.doLogin(bean);
        if(respEntity.isSuccess()){
            RedirectUtil.redirect(req, resp, "/home", Map.of("CURRENT_USER", respEntity.getData(),"MENU",MENU));
        }else{
            ViewUtil.render(req, resp, "error_page",Map.of("respEntity",respEntity));
        }

    }
    protected void _doPost( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String pwd_entrypted = req.getParameter("pwd_entrypted");

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
        // var loginResult = userService.doLogin(bean);
        // if(loginResult.isSuccess()){
        //     ViewUtil.render(req, resp, "example/index");
        // }else{
            
        // }
        logger.error("username :"+username);
        logger.error("password: "+password);
        logger.error("pwd_entrypted :"+pwd_entrypted);
        UserDTO signed = new UserDTO();
        signed.setUserAccount("Shengde.Yi");
        signed.setRoleId("Super Administrator");
        RedirectUtil.redirect(req, resp, "/home", Map.of("CURRENT_USER", signed,"MENU",MENU));
        // ViewUtil.render(req, resp, view,Map.of("CURRENT_USER",signed));
        
        // ViewUtil.render(req, resp, "error_page",Map.of("resp",RespEntity.error("错误错误！登录失败！无法登录！系统网络异常！")));
        
    }

}

// Java: Clean Workspace
