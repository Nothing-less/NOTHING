package icu.nothingless.controller.pages;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomePageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(HomePageServlet.class);
    private static final String HOME_PAGE = "home";
     @Override
    protected void doGet( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost( HttpServletRequest req,  HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> flashes =  RedirectUtil.getFlashes(req);
        flashes.forEach((k,v)->logger.info("K({}):V({})",k,v));
        if(flashes.size()>0){
            ViewUtil.render(req, resp, HOME_PAGE,flashes);
        }else{
            ViewUtil.render(req, resp, HOME_PAGE);
        }
    }

}
