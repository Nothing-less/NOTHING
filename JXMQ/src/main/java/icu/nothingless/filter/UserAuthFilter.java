package icu.nothingless.filter;

import java.io.IOException;
import java.util.Set;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.config.GlobalParams;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.RedirectUtil;
import icu.nothingless.tools.ViewUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(filterName = "UserAuthFilter", urlPatterns = "/home")
public class UserAuthFilter implements Filter {

    // 不需要拦截的资源
    private static final Set<String> EXCLUDE_URIS = Set.of(
            "/login",
            "/register",
            "/api/login",
            "/api/register",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico");

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        // 静态资源
        if (isExclude(uri)) {
            chain.doFilter(request, response);
            return;
        }

        Object obj = RedirectUtil.getFlash(req, GlobalParams.CURRENT_USER);
        if (obj == null) {
            // 未登录的请求
            ViewUtil.render(req, resp, GlobalParams.Pages.ERROR_PAGE,
                    java.util.Map.of("respEntity", RespEntity.unauthorized("未登录!")));
            return;
        }
        // 已登录的请求，继续处理
        User user = (User) obj;
        String userID = user.userId();
        String username = user.nickname();
        req.getSession(false).setAttribute(GlobalParams.CURRENT_USER_ID, userID);
        req.getSession(false).setAttribute(GlobalParams.CURRENT_USER_NICKNAME, username);
        // 已登录则放行
        chain.doFilter(request, response);
    }

    private boolean isExclude(String uri) {
        for (String exclude : EXCLUDE_URIS) {
            if (exclude.endsWith("/") && uri.startsWith(exclude)) {
                return true;
            }
            if (uri.equals(exclude)) {
                return true;
            }
        }
        return false;
    }
}
