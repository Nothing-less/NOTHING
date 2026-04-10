package icu.nothingless.controller.login;

import java.io.IOException;

import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LogoutServlet extends LoginServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            User currentUser = (User) session.getAttribute("CURRENT_USER");
            if (currentUser != null) {
                userService.doLogout(currentUser); // 这里可以根据实际情况传入用户信息
            }
            session.invalidate();
        }
        
        ViewUtil.render(request, response, "/example/index");
    }
}