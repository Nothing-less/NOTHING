package icu.nothingless.pojo.dto;

import java.util.Objects;
import icu.nothingless.pojo.bean.UserBean;

/**
 * User DTO - 用户数据传输对象
 */
public record User(
        String userId,
        String userAccount,
        String userPasswd,
        String nickname,
        String userInfos,
        String registerTime,
        String lastLoginTime,
        String lastLoginIpAddr,
        Boolean userStatus,
        String roleId,
        String userKey1,
        String userKey2,
        String userKey3,
        String userKey4,
        String userKey5,
        String userKey6
) implements java.io.Serializable{
    
    // ==================== 常量 ====================
    
    // 用户状态
    public static final boolean STATUS_ACTIVE = true;
    public static final boolean STATUS_INACTIVE = false;
    
    // ==================== 紧凑构造器（校验 & 默认值）====================
    
    public User {
        // 自动 trim
        if (userId != null) userId = userId.trim();
        if (userAccount != null) userAccount = userAccount.trim().toLowerCase();
        if (nickname != null) nickname = nickname.trim();
        if (roleId != null) roleId = roleId.trim();
        
        // 默认值
        if (userStatus == null) userStatus = STATUS_ACTIVE;
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 是否激活
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.userStatus);
    }
    
    /**
     * 脱敏后的账号（用于展示）
     */
    public String getMaskedAccount() {
        if (userAccount == null || userAccount.length() <= 4) return userAccount;
        return userAccount.substring(0, 2) + "****" + 
               userAccount.substring(userAccount.length() - 2);
    }
    
    /**
     * 清除敏感字段（返回给前端时用）
     */
    public User withoutPasswd() {
        return this.toBuilder().userPasswd(null).build();
    }
    
    // ==================== Builder 模式 ====================
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 从当前对象创建 Builder（用于修改副本）
     */
    public Builder toBuilder() {
        return new Builder()
                .userId(this.userId)
                .userAccount(this.userAccount)
                .userPasswd(this.userPasswd)
                .nickname(this.nickname)
                .userInfos(this.userInfos)
                .registerTime(this.registerTime)
                .lastLoginTime(this.lastLoginTime)
                .lastLoginIpAddr(this.lastLoginIpAddr)
                .userStatus(this.userStatus)
                .roleId(this.roleId)
                .userKey1(this.userKey1)
                .userKey2(this.userKey2)
                .userKey3(this.userKey3)
                .userKey4(this.userKey4)
                .userKey5(this.userKey5)
                .userKey6(this.userKey6);
    }
    
    public static class Builder {
        private String userId;
        private String userAccount;
        private String userPasswd;
        private String nickname;
        private String userInfos;
        private String registerTime;
        private String lastLoginTime;
        private String lastLoginIpAddr;
        private Boolean userStatus;
        private String roleId;
        private String userKey1;
        private String userKey2;
        private String userKey3;
        private String userKey4;
        private String userKey5;
        private String userKey6;
        
        private Builder() {}
        
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder userAccount(String userAccount) { this.userAccount = userAccount; return this; }
        public Builder userPasswd(String userPasswd) { this.userPasswd = userPasswd; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder userInfos(String userInfos) { this.userInfos = userInfos; return this; }
        public Builder registerTime(String registerTime) { this.registerTime = registerTime; return this; }
        public Builder lastLoginTime(String lastLoginTime) { this.lastLoginTime = lastLoginTime; return this; }
        public Builder lastLoginIpAddr(String lastLoginIpAddr) { this.lastLoginIpAddr = lastLoginIpAddr; return this; }
        public Builder userStatus(Boolean userStatus) { this.userStatus = userStatus; return this; }
        public Builder roleId(String roleId) { this.roleId = roleId; return this; }
        public Builder userKey1(String userKey1) { this.userKey1 = userKey1; return this; }
        public Builder userKey2(String userKey2) { this.userKey2 = userKey2; return this; }
        public Builder userKey3(String userKey3) { this.userKey3 = userKey3; return this; }
        public Builder userKey4(String userKey4) { this.userKey4 = userKey4; return this; }
        public Builder userKey5(String userKey5) { this.userKey5 = userKey5; return this; }
        public Builder userKey6(String userKey6) { this.userKey6 = userKey6; return this; }
        
        /**
         * 从 Entity 构建
         */
        public Builder from(UserBean bean) {
            this.userId = bean.getUserId();
            this.userAccount = bean.getUserAccount();
            this.userPasswd = bean.getUserPasswd();
            this.nickname = bean.getNickname();
            this.userInfos = bean.getUserInfos();
            this.registerTime = bean.getRegisterTime();
            this.lastLoginTime = bean.getLastLoginTime();
            this.lastLoginIpAddr = bean.getLastLoginIpAddr();
            this.userStatus = bean.getUserStatus();
            this.roleId = bean.getRoleId();
            this.userKey1 = bean.getUserKey1();
            this.userKey2 = bean.getUserKey2();
            this.userKey3 = bean.getUserKey3();
            this.userKey4 = bean.getUserKey4();
            this.userKey5 = bean.getUserKey5();
            this.userKey6 = bean.getUserKey6();
            return this;
        }
        
        /**
         * 设置最后登录信息
         */
        public Builder loginNow(String ipAddr) {
            this.lastLoginTime = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.lastLoginIpAddr = ipAddr;
            return this;
        }
        
        public User build() {
            // 必填校验
            if (userId == null || userId.isEmpty()) {
                throw new IllegalStateException("userId is required");
            }
            if (userAccount == null || userAccount.isEmpty()) {
                throw new IllegalStateException("userAccount is required");
            }
            return new User(userId, userAccount, userPasswd, nickname, userInfos,
                    registerTime, lastLoginTime, lastLoginIpAddr, userStatus, roleId,
                    userKey1, userKey2, userKey3, userKey4, userKey5, userKey6);
        }
    }
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 从 Entity 快速创建
     */
    public static User from(UserBean bean) {
        return User.builder().from(bean).build();
    }
    
    /**
     * 创建用于更新的 DTO（只包含可修改字段）
     */
    public static User forUpdate(String userId, String nickname, String userInfos) {
        return User.builder()
                .userId(userId)
                .nickname(nickname)
                .userInfos(userInfos)
                .build();
    }
    
    /**
     * 创建列表展示用的精简 DTO
     */
    public static User forList(String userId, String userAccount, String nickname, 
                               Boolean userStatus, String roleId) {
        return User.builder()
                .userId(userId)
                .userAccount(userAccount)
                .nickname(nickname)
                .userStatus(userStatus)
                .roleId(roleId)
                .build();
    }
    
    // ==================== Object 方法 ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(userAccount, that.userAccount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, userAccount);
    }
    
    @Override
    public String toString() {
        return "User{" +
               "userId='" + userId + '\'' +
               ", userAccount='" + userAccount + '\'' +
               ", userPasswd='[PROTECTED]'" +
               ", nickname='" + nickname + '\'' +
               ", userStatus=" + userStatus +
               ", roleId='" + roleId + '\'' +
               '}';
    }
}