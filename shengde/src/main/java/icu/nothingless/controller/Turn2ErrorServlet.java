package icu.nothingless.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Turn2ErrorServlet extends HttpServlet {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Turn2ErrorServlet.class);

    @Override
    protected void doGet( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        logger.error("Error occurred !");
        logger.error("Error message:",req.getAttribute("respEntity"));
        ViewUtil.render(req, resp, "error_page",Map.of("respEntity",RespEntity.error("错误错误！登录失败！无法登录！系统网络异常！")));
    }
}
