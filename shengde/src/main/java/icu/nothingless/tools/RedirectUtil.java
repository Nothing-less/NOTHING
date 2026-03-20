package icu.nothingless.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class RedirectUtil {
    
    private static final String PREFIX = "FLASH_";
    
    /**
     * 设置 Flash 消息并重定向
     */
    public static void redirect(HttpServletRequest req, HttpServletResponse resp, 
                                         String targetPath, Map<String, Object> flashData) throws IOException {
        if (flashData != null) {
            HttpSession session = req.getSession();
            flashData.forEach((key, value) -> session.setAttribute(PREFIX + key, value));
        }
        String path = targetPath.startsWith("/") ? targetPath : "/" + targetPath;
        resp.sendRedirect(req.getContextPath() + path);
    }
    
    /**
     * 获取 Flash 数据并自动清理（返回 Map，一次性取完）
     */
    public static Map<String, Object> getFlashes(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return Collections.emptyMap();
        
        Map<String, Object> result = new HashMap<>();
        List<String> keysToRemove = new ArrayList<>();
        
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith(PREFIX)) {
                result.put(name.substring(PREFIX.length()), session.getAttribute(name));
                keysToRemove.add(name);
            }
        }
        
        // 一次性清理
        keysToRemove.forEach(session::removeAttribute);
        return result;
    }
    
    /**
     * 获取单个 Flash 值（按需取用）
     */
    public static Object getFlash(HttpServletRequest req, String key) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        
        Object value = session.getAttribute(PREFIX + key);
        session.removeAttribute(PREFIX + key);
        return value;
    }
}