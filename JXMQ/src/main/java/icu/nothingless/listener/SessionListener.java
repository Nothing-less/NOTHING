package icu.nothingless.listener;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class SessionListener implements HttpSessionListener {
    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);

    // userId → Session 的映射
    private static final ConcurrentHashMap<String, HttpSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.info("Session 创建: " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        logger.info("Session 销毁: " + se.getSession().getId());
    }

    /**
     * 用户登录时调用：绑定用户和新Session，踢掉旧的
     */
    public static void bindUserSession(String userId, HttpSession newSession) {
        HttpSession oldSession = SESSION_MAP.put(userId, newSession);
        if (oldSession != null && oldSession != newSession) {
            try {
                // 标记旧会话被踢出
                oldSession.setAttribute("SESSION_KICKED", true);
                // oldSession.invalidate(); // 使旧会话失效
            } catch (IllegalStateException e) {
                logger.warn("旧会话已失效，无法踢出: " + oldSession.getId());
            }
        }
    }

    /**
     * 用户退出时调用
     */
    public static void removeUserSession(String userId) {
        try {
            SESSION_MAP.remove(userId);
        } catch (Exception e) {
            logger.warn("会话已失效，无法移除: " + userId);
        }
    }

}
