package icu.nothingless.controller.pages;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.service.interfaces.iPageService;
import icu.nothingless.tools.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 菜单API - 返回JSON格式的菜单数据供home.jsp使用
 */
@WebServlet("/api/menu")
public class PageRouteServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PageRouteServlet.class);
    private static final Gson gson = new Gson();
    
    // 缓存的菜单数据
    private static List<MenuItem> CACHED_MENU = new java.util.ArrayList<>();
    
    // 默认菜单（当配置加载失败时使用）
    private static final List<MenuItem> DEFAULT_MENU = List.of(
        // new MenuItem("dashboard", "主页"),
        // new MenuItem("users", "用户管理"),
        // new MenuItem("orders", "订单管理"),
        // new MenuItem("products", "商品管理"),
        // new MenuItem("analytics", "数据分析"),
        // new MenuItem("example_tables", "数据表门"),
        // new MenuItem("settings", "系统设置")
    );
    
    static {
        // loadMenuCache();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // 支持CORS（如果需要）
        resp.setHeader("Access-Control-Allow-Origin", "*");
        
        PrintWriter out = resp.getWriter();
        
        try {
            // 如果缓存为空，尝试重新加载
            if (CACHED_MENU == null || CACHED_MENU.isEmpty()) {
                loadMenuCache();
            }
            
            List<MenuItem> menuToReturn = (CACHED_MENU != null) ? CACHED_MENU : DEFAULT_MENU;
            
            // 返回统一格式的JSON响应
            RespEntity response = RespEntity.success(menuToReturn);
            out.print(gson.toJson(response));
            
        } catch (Exception e) {
            logger.error("Failed to return menu data", e);
            RespEntity errorResponse = RespEntity.error("菜单加载失败");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(errorResponse));
        }
    }
    
    /**
     * 重新加载菜单缓存
     */
    private static synchronized void loadMenuCache() {
        try {
            iPageService pageService = ServiceFactory.getSingleton(iPageService.class);
            Set<Map<String, String>> pages = pageService.getPages("main_page");
            
            if (pages != null && !pages.isEmpty()) {
                pages.forEach(
                    page->CACHED_MENU.add(new MenuItem(page.get("page_link"), page.get("page_name")))
                );
                logger.info("Loaded {} menu items", CACHED_MENU.size());
            } else {
                logger.warn("No menu items loaded, using default");
                CACHED_MENU = DEFAULT_MENU;
            }
            
        } catch (Exception e) {
            logger.error("Failed to load menu from service", e);
            CACHED_MENU = DEFAULT_MENU;
        }
    }
    
    /**
     * 菜单项数据类
     */
    public record MenuItem(String name, String displayText) {}
}