package icu.nothingless.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String text = now.format(fmt);
        String token = "Testing token" + text;
        iUserSTOAdapter bean = new UserSTO();
        bean.setUserAccount(username);
        bean.setUserPasswd(password);
        // bean.setRegisterTime(text);
        // bean.setLastLoginIpAddr("127.0.0.1");
        // bean.setLastLoginTime(text);
        // bean.setRoleId("Admin");

        List<iUserSTOAdapter> result = bean.query();
        if (!result.isEmpty()) {
            result.forEach(System.out::println);
        }
    }

}
