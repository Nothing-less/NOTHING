package icu.nothingless.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import icu.nothingless.dto.UserAction;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UserBehaviorFilter {

    // 内存队列缓冲（后续改成Redis）
    private static final BlockingQueue<UserAction> LOG_QUEUE = new ArrayBlockingQueue<>(10000);
    private ExecutorService executorService;
    private volatile boolean running = true;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final Logger logger = LoggerFactory.getLogger(UserBehaviorFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // 启动异步消费线程（模拟 AOP 后置处理）
        executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> {
            while (running) {
                try {
                    UserAction action = LOG_QUEUE.take();
                    // 实际存储：控制台、文件或数据库
                    saveToStorage(action);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        System.out.println("UserBehavior AOP Filter initialized");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String uri = httpReq.getRequestURI();

        // AOP 切入点：只拦截监控上报接口
        if (uri.equals("/api/log/collect")) {
            handleMonitorRequest(httpReq, httpResp);
            return; // 不继续转发，直接结束
        }

        // AOP 前置通知：记录请求进入（可选）
        long startTime = System.currentTimeMillis();

        try {
            // 继续执行原请求（ ProceedingJoinPoint.proceed() 的等价物）
            chain.doFilter(request, response);
        } finally {
            // AOP 后置通知：记录请求耗时（可选）
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) { // 慢查询监控
                System.out.printf("[SLOW REQUEST] %s took %dms%n", uri, duration);
            }
        }
    }

    private void handleMonitorRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // 读取前端上报的 JSON 数据
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(req.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        // 解析并封装（简化版，实际可用 Gson/Jackson）
        UserAction action = parseAction(sb.toString(), req);

        // 放入异步队列（非阻塞）
        if (!LOG_QUEUE.offer(action)) {
            System.err.println("Monitor queue full, dropping log");
        }

        // 立即返回 204 No Content，不影响前端
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private UserAction parseAction(String json, HttpServletRequest req) {
        try {
            // 反序列化 JSON 到 Record
            UserAction parsed = objectMapper.readValue(json, UserAction.class);

            // 补充 HTTP 请求中的字段（IP、UA 等不能被客户端伪造的字段）
            // Record 不可变，只能基于已有数据创建新的
            return new UserAction(
                    parsed.audit(), // 保持原有审计信息
                    parsed.type(),
                    parsed.location(),
                    parsed.target(),
                    parsed.input(),
                    // 使用服务端获取的真实数据
                    new UserAction.ClientEnvironment(
                            req.getHeader("User-Agent"), // 真实 UA，不信任客户端
                            getClientIP(req), // real IP
                            parsed.client().screenResolution(),
                            parsed.client().referrer()),
                    parsed.principal(),
                    parsed.metrics());

        } catch (JsonProcessingException e) {
            // 解析失败返回最小化对象或抛出异常
            return UserAction.builder()
                    .sessionId("error_" + System.currentTimeMillis())
                    .type(UserAction.ActionType.PAGE_ENTER)
                    .timestamp(Instant.now())
                    .location("error", "/error")
                    .client(req.getHeader("User-Agent"), getClientIP(req), "unknown", null)
                    .build();
        }
    }

    private void saveToStorage(UserAction action) {
        try {
            // 记录业务日志（脱敏）
            if (logger.isInfoEnabled()) {
                logger.info("UserAction[type={}, session={}, user={}, ipMasked={}, sensitive={}]",
                        action.type(),
                        action.audit().sessionId(),
                        action.principal().map(UserAction.UserIdentity::username).orElse("guest"),
                        maskIp(action.client().ipAddress()),
                        action.isSensitive());
            }

            // 敏感操作单独记录到安全审计日志（使用不同的 logger）
            if (action.isSensitive()) {
                Logger auditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
                auditLogger.warn("Sensitive action detected: session={}, type={}, element={}",
                        action.audit().sessionId(),
                        action.type(),
                        action.target().flatMap(UserAction.ElementTarget::id).orElse("unknown"));
            }

        } catch (Exception e) {
            // 记录错误，但不影响主业务流程
            logger.error("Failed to log UserAction for session={}",
                    action != null ? action.audit().sessionId() : "null",
                    e);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String maskIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "unknown";
        }
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.* ";
        }
        return "***";
    }

    public void destroy() {
        running = false;
        executorService.shutdown();
    }
}
