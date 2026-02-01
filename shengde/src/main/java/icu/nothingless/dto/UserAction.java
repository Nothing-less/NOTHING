package icu.nothingless.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * 用户行为追踪 DTO（优化版）
 * 
 * 设计原则：
 * 1. Record 自动提供 equals/hashCode/toString，无需手动实现
 * 2. 使用 Instant 替代 String 时间戳（类型安全 + 时区无关）
 * 3. 19个字段按语义分组为嵌套 Record（避免参数爆炸）
 * 4. 必填字段使用紧凑构造函数验证
 * 5. 敏感数据通过工厂方法控制创建
 */
public record UserAction(
        // 核心标识（必填）
        AuditContext audit,

        // 操作类型（必填）
        ActionType type,

        // 页面位置
        PageLocation location,

        // 目标元素（可选，点击/输入时有值）
        Optional<ElementTarget> target,

        // 输入数据（可选，敏感数据自动脱敏）
        Optional<InputSnapshot> input,

        // 客户端环境
        ClientEnvironment client,

        // 用户身份（未登录时为 empty）
        Optional<UserIdentity> principal,

        // 性能指标
        PerformanceMetrics metrics) {

    // ========== 紧凑构造函数：验证与默认值 ==========
    public UserAction {
        Objects.requireNonNull(audit, "审计上下文不能为空");
        Objects.requireNonNull(type, "操作类型不能为空");

        // 默认当前时间（如果未提供）
        if (audit.timestamp() == null) {
            audit = new AuditContext(audit.sessionId(), Instant.now());
        }

        // Optional 字段规范化（避免 null）
        target = target != null ? target : Optional.empty();
        input = input != null ? input : Optional.empty();
        principal = principal != null ? principal : Optional.empty();
    }

    // ========== 业务辅助方法 ==========

    /**
     * 是否为敏感操作（密码输入、含掩码字符等）
     */
    public boolean isSensitive() {
        return input.map(InputSnapshot::isMasked).orElse(false) ||
                target.map(ElementTarget::isSensitiveInput).orElse(false);
    }

    /**
     * 是否为已登录用户
     */
    public boolean isAuthenticated() {
        return principal.isPresent();
    }

    /**
     * 获取简短描述（用于日志）
     */
    public String summary() {
        String targetDesc = target
                .map(t -> t.id().orElse(t.tag()))
                .orElse("page");
        return "[%s] %s on %s".formatted(type, targetDesc, location.path());
    }

    // ========== 嵌套 Record 定义 ==========

    /**
     * 审计上下文（核心必填信息）
     */
    public record AuditContext(
            String sessionId,
            Instant timestamp) {
        public AuditContext {
            Objects.requireNonNull(sessionId, "SessionId 不能为空");
            // timestamp 允许 null（由外层设置默认值）
        }
    }

    /**
     * 操作类型（使用 enum 限定取值范围）
     */
    public enum ActionType {
        CLICK, INPUT, PAGE_ENTER, PAGE_LEAVE, SCROLL, SUBMIT, API_CALL
    }

    /**
     * 页面位置
     */
    public record PageLocation(
            String url, // 完整 URL，如 https://example.com/login?redirect=home
            String path // 路径部分，如 /login
    ) {
        public PageLocation {
            Objects.requireNonNull(path, "页面路径不能为空");
        }
    }

    /**
     * 目标元素（点击/输入对象）
     */
    public record ElementTarget(
            String tag, // BUTTON, INPUT 等
            Optional<String> id, // 元素 ID
            Optional<String> name, // name 属性
            Optional<String> cssClass, // CSS 类名
            Optional<String> inputType, // text/password/email 等
            Optional<String> cssSelector // 选择器路径
    ) {
        public ElementTarget {
            Objects.requireNonNull(tag, "元素标签不能为空");
            id = id != null ? id : Optional.empty();
            name = name != null ? name : Optional.empty();
            cssClass = cssClass != null ? cssClass : Optional.empty();
            inputType = inputType != null ? inputType : Optional.empty();
            cssSelector = cssSelector != null ? cssSelector : Optional.empty();
        }

        public boolean isSensitiveInput() {
            return inputType
                    .map(t -> t.equalsIgnoreCase("password") ||
                            t.equalsIgnoreCase("credit-card") ||
                            t.equalsIgnoreCase("cvv"))
                    .orElse(false);
        }
    }

    /**
     * 输入数据快照（重点关注：安全处理）
     */
    public record InputSnapshot(
            String displayValue, // 脱敏后的值（如 ******）
            Optional<String> encryptedValue, // 加密后的原始值（审计用）
            boolean isMasked) {
        public InputSnapshot {
            Objects.requireNonNull(displayValue, "显示值不能为空");
            encryptedValue = encryptedValue != null ? encryptedValue : Optional.empty();
        }

        /**
         * 工厂方法：创建脱敏的输入数据
         * 自动将原始值替换为星号，并"假装"加密（实际项目接入加密服务）
         */
        public static InputSnapshot mask(String originalValue) {
            if (originalValue == null || originalValue.isEmpty()) {
                return new InputSnapshot("", Optional.empty(), false);
            }
            String masked = "*".repeat(Math.min(originalValue.length(), 6));
            String encrypted = "ENC:" + Integer.toHexString(originalValue.hashCode());
            return new InputSnapshot(masked, Optional.of(encrypted), true);
        }

        /**
         * 工厂方法：明文输入（仅用于非敏感字段如搜索框）
         */
        public static InputSnapshot plain(String value) {
            return new InputSnapshot(
                    value != null ? value : "",
                    Optional.empty(),
                    false);
        }
    }

    /**
     * 客户端环境
     */
    public record ClientEnvironment(
            String userAgent,
            String ipAddress,
            String screenResolution, // 1920x1080
            Optional<String> referrer) {
        public ClientEnvironment {
            Objects.requireNonNull(ipAddress, "IP 地址不能为空");
            referrer = referrer != null ? referrer : Optional.empty();
        }
    }

    /**
     * 用户身份（登录信息）
     */
    public record UserIdentity(
            Long id,
            String username) {
        public boolean isAnonymous() {
            return id == null;
        }
    }

    /**
     * 性能指标
     */
    public record PerformanceMetrics(
            Optional<Long> stayTimeMillis, // 页面停留时长
            Optional<Long> responseTimeMillis // 服务端处理耗时
    ) {
        public PerformanceMetrics {
            stayTimeMillis = stayTimeMillis != null ? stayTimeMillis : Optional.empty();
            responseTimeMillis = responseTimeMillis != null ? responseTimeMillis : Optional.empty();
        }

        public static PerformanceMetrics of(Long stay, Long response) {
            return new PerformanceMetrics(
                    Optional.ofNullable(stay),
                    Optional.ofNullable(response));
        }
    }

    // ========== 构建器 ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId;
        private ActionType type;
        private Instant timestamp;
        private String pageUrl;
        private String pagePath;
        private ElementTarget target;
        private InputSnapshot input;
        private ClientEnvironment client;
        private UserIdentity principal;
        private PerformanceMetrics metrics;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder type(ActionType type) {
            this.type = type;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder timestamp(String iso8601) {
            this.timestamp = iso8601 != null ? Instant.parse(iso8601) : null;
            return this;
        }

        public Builder location(String url, String path) {
            this.pageUrl = url;
            this.pagePath = path;
            return this;
        }

        public Builder target(ElementTarget target) {
            this.target = target;
            return this;
        }

        public Builder target(String tag, String id, String name, String cssClass, String inputType, String selector) {
            this.target = new ElementTarget(
                    tag,
                    Optional.ofNullable(id),
                    Optional.ofNullable(name),
                    Optional.ofNullable(cssClass),
                    Optional.ofNullable(inputType),
                    Optional.ofNullable(selector));
            return this;
        }

        public Builder input(InputSnapshot input) {
            this.input = input;
            return this;
        }

        public Builder input(String value, boolean sensitive) {
            this.input = sensitive ? InputSnapshot.mask(value) : InputSnapshot.plain(value);
            return this;
        }

        public Builder client(String userAgent, String ip, String screenSize, String referrer) {
            this.client = new ClientEnvironment(
                    userAgent, ip, screenSize, Optional.ofNullable(referrer));
            return this;
        }

        public Builder principal(Long id, String username) {
            this.principal = new UserIdentity(id, username);
            return this;
        }

        public Builder metrics(Long stayTime, Long responseTime) {
            this.metrics = PerformanceMetrics.of(stayTime, responseTime);
            return this;
        }

        public UserAction build() {
            return new UserAction(
                    new AuditContext(sessionId, timestamp),
                    type,
                    new PageLocation(pageUrl, pagePath),
                    Optional.ofNullable(target),
                    Optional.ofNullable(input),
                    client,
                    Optional.ofNullable(principal),
                    metrics != null ? metrics : new PerformanceMetrics(Optional.empty(), Optional.empty()));
        }
    }
}