package icu.nothingless.controller;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        // 获取表单数据
        String username = req.getParameter("username");
        //tring password = req.getParameter("password");

            req.getSession().setAttribute("token", username.hashCode() + ":" + System.currentTimeMillis());
            String targetPage = "/WEB-INF/jsp/test.jsp";
            req.getRequestDispatcher(targetPage).forward(req, resp);

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        super.doOptions(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        super.doPut(req, resp);
    }

    @Override
    public void destroy() {

        super.destroy();
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj);
    }

    @Override
    public String toString() {

        return super.toString();
    }
    
}
