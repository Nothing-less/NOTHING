package icu.nothingless.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.controller.pages.PageRouteServlet.MenuItem;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.JsonUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 菜单权限过滤器
 * 拦截 /api/menu 请求，根据用户角色过滤菜单项
 */
@WebFilter(urlPatterns = "/api/menu")
public class MenuAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MenuAuthFilter.class);

    // 权限配置：page_link -> 允许访问的角色列表
    // 特殊权限用户可见的页面
    private static final Map<String, List<String>> PAGE_ROLE_ACCESS = new HashMap<>();

    static {
        PAGE_ROLE_ACCESS.put("user_create", Arrays.asList(User.ROLE_SUPER_ADMIN));
        // 其他需要权限控制的页面
        // PAGE_ROLE_ACCESS.put("system_config", Arrays.asList("Super Administrator",
        // "Admin"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 先让请求继续执行，获取原始菜单数据
        MenuResponseWrapper wrapper = new MenuResponseWrapper(resp);
        chain.doFilter(req, wrapper);

        // 获取响应内容
        String jsonResponse = wrapper.getCaptureAsString();
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            resp.getWriter().write(jsonResponse);
            return;
        }

        try {
            // 解析原始菜单响应
            java.lang.reflect.Type respType = new TypeToken<RespEntity<List<MenuItem>>>() {
            }.getType();
            RespEntity<List<MenuItem>> rawResponse = JsonUtil.fromJson(jsonResponse, respType);
            if (rawResponse == null || rawResponse.getData() == null) {
                resp.getWriter().write(jsonResponse);
                return;
            }

            // 获取当前用户角色
            String roleId = getCurrentUserRole(req);

            // 过滤菜单
            List<MenuItem> rawMenu = (List<MenuItem>) rawResponse.getData();
            if (rawMenu == null || rawMenu.isEmpty()) {
                resp.getWriter().write(jsonResponse);
                return;
            }
            List<MenuItem> filteredMenu = filterMenuByRole(rawMenu, roleId);

            // 重新构建响应
            RespEntity<List<MenuItem>> newResponse = RespEntity.success(filteredMenu);
            String newJson = JsonUtil.toJson(newResponse);

            resp.setContentType("application/json;charset=UTF-8");
            resp.setContentLength(newJson.getBytes("UTF-8").length);
            resp.getWriter().write(newJson);

        } catch (RuntimeException e) {
            logger.error("Menu filter failed", e);
            resp.getWriter().write(jsonResponse); // 失败时返回原始数据
        }
    }

    private String getCurrentUserRole(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null)
            return null;
        User currentUser = (User) icu.nothingless.tools.RedirectUtil.getFlash(req, "CURRENT_USER");
        if (currentUser == null)
            return null;
        Object roleId = currentUser.roleId();
        return roleId != null ? String.valueOf(roleId) : null;
    }

    private List<MenuItem> filterMenuByRole(List<MenuItem> rawMenu, String roleId) {
        List<MenuItem> result = new java.util.ArrayList<>();

        for (Object item : rawMenu) {
            if (!(item instanceof MenuItem))
                continue;

            MenuItem menuItem = (MenuItem) item;
            String pageLink = menuItem.name(); // MenuItem record 的 name 字段

            // 检查权限
            if (PAGE_ROLE_ACCESS.containsKey(pageLink)) {
                List<String> allowedRoles = PAGE_ROLE_ACCESS.get(pageLink);
                // 有权限，转换为正确格式
                if (allowedRoles.contains(roleId)) {
                    result.add(menuItem);
                }
                // 无权限，跳过
            } else {
                // 无权限配置，所有角色可见
                result.add(menuItem);
            }
        }
        return result;
    }
}