package icu.nothingless.filter;

import java.io.IOException;
import java.util.Map;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.config.GlobalParams;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
// import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// @WebFilter(urlPatterns = {"/home", "/page/*", "/api/*"})
public class SessionCheckFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);

        if (session != null) {
            Boolean kicked = (Boolean) session.getAttribute("SESSION_KICKED");
            if (kicked != null && kicked) {
                session.invalidate();
                ViewUtil.render(request, response, GlobalParams.Pages.ERROR_PAGE,
                        Map.of("respEntity", RespEntity.error("其他账号已登录，您已被踢出，请重新登录")));
                return;
            }
        }
        chain.doFilter(req, resp);
    }
}
