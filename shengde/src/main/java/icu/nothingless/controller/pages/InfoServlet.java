package icu.nothingless.controller.pages;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.dto.UserDTO;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "InfoServlet", urlPatterns = "/api/user")
public class InfoServlet extends HttpServlet {
    
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = req.getSession(false);
        UserDTO user = session != null ? (UserDTO) session.getAttribute("CURRENT_USER") : null;
        
        if (user == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(RespEntity.error("未登录")));
            return;
        }
        
        // 只返回必要字段，过滤敏感信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("userAccount", user.getUserAccount());
        userInfo.put("roleId", user.getRoleId());
        // 不要返回密码！
        
        resp.getWriter().write(gson.toJson(RespEntity.success(userInfo)));
    }
}
