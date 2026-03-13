package icu.nothingless.tools;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Session传递工具类
 * 处理重定向时的Session传递，原Session失效，新Session自动创建
 */
public class SessionTransferUtil {
    
    // Session传递标识的key
    private static final String SESSION_TRANSFER_FLAG = "_SESSION_TRANSFER_FLAG_";
    private static final String TRANSFERRED_DATA_PREFIX = "_TRANSFERRED_";
    
    /**
     * 重定向并传递Session
     * @param request 当前请求对象
     * @param response 当前响应对象
     * @param targetPath 目标路径（相对路径，如：/str2）
     * @throws IOException 重定向异常
     */
    public static void redirect(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  String targetPath) throws IOException {
        
        // 1. 获取当前Session（如果存在）
        HttpSession currentSession = request.getSession(false);
        
        // 2. 格式化目标路径
        if (targetPath == null || targetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("目标路径不能为空");
        }
        
        // 确保路径以/开头
        String formattedPath = targetPath.startsWith("/") ? targetPath : "/" + targetPath;
        
        // 3. 如果有Session，则传递数据
        if (currentSession != null && !currentSession.isNew()) {
            // 生成唯一的传递标识
            String transferId = UUID.randomUUID().toString();
            
            // 复制当前Session的所有属性到临时存储
            Map<String, Object> sessionData = copySessionAttributes(currentSession);
            
            // 将传递标识和数据存储到当前Session
            currentSession.setAttribute(SESSION_TRANSFER_FLAG, transferId);
            currentSession.setAttribute(transferId, sessionData);
            
            // 设置Session立即失效（可选）
            currentSession.invalidate();
        }
        
        // 4. 执行重定向
        response.sendRedirect(formattedPath);
    }
    
    /**
     * 在目标Servlet中恢复传递的Session数据
     * @param request 目标请求对象
     * @return 是否成功恢复了Session数据
     */
    public static boolean restoreTransferredSession(HttpServletRequest request) {
        HttpSession newSession = request.getSession();
        
        // 获取传递标识
        String transferId = (String) newSession.getAttribute(SESSION_TRANSFER_FLAG);
        if (transferId == null) {
            return false;
        }
        
        // 获取传递的数据
        @SuppressWarnings("unchecked")
        Map<String, Object> transferredData = (Map<String, Object>) newSession.getAttribute(transferId);
        
        if (transferredData != null) {
            // 恢复数据到新Session
            for (Map.Entry<String, Object> entry : transferredData.entrySet()) {
                if (!entry.getKey().startsWith("_")) { // 不恢复内部属性
                    newSession.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            
            // 清理临时数据
            newSession.removeAttribute(SESSION_TRANSFER_FLAG);
            newSession.removeAttribute(transferId);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 重定向并选择性传递Session属性
     * @param request 当前请求对象
     * @param response 当前响应对象
     * @param targetPath 目标路径
     * @param attributeKeys 要传递的属性名数组（null表示传递所有）
     * @throws IOException 重定向异常
     */
    public static void redirectWithSelectedSession(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  String targetPath,
                                                  String... attributeKeys) throws IOException {
        
        HttpSession currentSession = request.getSession(false);
        
        // 格式化路径
        if (!targetPath.startsWith("/")) {
            targetPath = "/" + targetPath;
        }
        
        if (currentSession != null && !currentSession.isNew()) {
            String transferId = UUID.randomUUID().toString();
            Map<String, Object> selectedData = new HashMap<>();
            
            if (attributeKeys != null && attributeKeys.length > 0) {
                // 只传递指定的属性
                for (String key : attributeKeys) {
                    Object value = currentSession.getAttribute(key);
                    if (value != null) {
                        selectedData.put(key, value);
                    }
                }
            } else {
                // 传递所有属性
                selectedData = copySessionAttributes(currentSession);
            }
            
            // 存储传递数据
            currentSession.setAttribute(SESSION_TRANSFER_FLAG, transferId);
            currentSession.setAttribute(transferId, selectedData);
        }
        
        response.sendRedirect(targetPath);
    }
    
    /**
     * 重定向并使原Session失效
     * @param request 当前请求对象
     * @param response 当前响应对象
     * @param targetPath 目标路径
     * @throws IOException 重定向异常
     */
    public static void redirectWithSessionInvalidate(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    String targetPath) throws IOException {
        
        HttpSession currentSession = request.getSession(false);
        
        if (!targetPath.startsWith("/")) {
            targetPath = "/" + targetPath;
        }
        
        if (currentSession != null) {
            String transferId = UUID.randomUUID().toString();
            Map<String, Object> sessionData = copySessionAttributes(currentSession);
            
            // 存储数据
            currentSession.setAttribute(SESSION_TRANSFER_FLAG, transferId);
            currentSession.setAttribute(transferId, sessionData);
            
            // 使原Session失效
            currentSession.invalidate();
        }
        
        response.sendRedirect(targetPath);
    }
    
    /**
     * 检查是否有传递的Session数据
     * @param request 请求对象
     * @return 是否有传递的数据
     */
    public static boolean hasTransferredSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_TRANSFER_FLAG) != null;
    }
    
    /**
     * 获取传递的特定属性
     * @param request 请求对象
     * @param key 属性名
     * @param <T> 属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTransferredAttribute(HttpServletRequest request, String key) {
        if (hasTransferredSession(request)) {
            restoreTransferredSession(request);
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (T) session.getAttribute(key);
        }
        return null;
    }
    
    /**
     * 清理所有传递相关的临时数据
     * @param request 请求对象
     */
    public static void cleanupTransferData(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String transferId = (String) session.getAttribute(SESSION_TRANSFER_FLAG);
            if (transferId != null) {
                session.removeAttribute(SESSION_TRANSFER_FLAG);
                session.removeAttribute(transferId);
            }
            
            // 清理所有前缀为_TRANSFERRED_的属性
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                if (name.startsWith(TRANSFERRED_DATA_PREFIX)) {
                    session.removeAttribute(name);
                }
            }
        }
    }
    
    /**
     * 复制Session的所有属性
     */
    private static Map<String, Object> copySessionAttributes(HttpSession session) {
        Map<String, Object> data = new HashMap<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            // 不复制内部属性
            if (!name.startsWith("_")) {
                data.put(name, session.getAttribute(name));
            }
        }
        
        return data;
    }
}