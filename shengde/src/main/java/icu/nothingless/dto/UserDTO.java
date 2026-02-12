package icu.nothingless.dto;

import java.util.Objects;

/**
 * 用户数据传输对象 (User Data Transfer Object)
 * 纯Java实现，零依赖，支持Builder模式和MapStruct映射
 */
public class UserDTO {
    
    // ==================== 字段 ====================
    
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
    
    // ==================== 构造方法 ====================
    
    /**
     * 无参构造 - 供MapStruct和反射使用
     */
    public UserDTO() {
    }
    
    /**
     * 全参构造 - 供Builder使用
     */
    private UserDTO(Builder builder) {
        this.userId = builder.userId;
        this.userAccount = builder.userAccount;
        this.userPasswd = builder.userPasswd;
        this.nickname = builder.nickname;
        this.userInfos = builder.userInfos;
        this.registerTime = builder.registerTime;
        this.lastLoginTime = builder.lastLoginTime;
        this.lastLoginIpAddr = builder.lastLoginIpAddr;
        this.userStatus = builder.userStatus;
        this.roleId = builder.roleId;
        this.userKey1 = builder.userKey1;
        this.userKey2 = builder.userKey2;
        this.userKey3 = builder.userKey3;
        this.userKey4 = builder.userKey4;
        this.userKey5 = builder.userKey5;
        this.userKey6 = builder.userKey6;
    }
    
    // ==================== Getter & Setter ====================
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserAccount() { return userAccount; }
    public void setUserAccount(String userAccount) { this.userAccount = userAccount; }
    
    public String getUserPasswd() { return userPasswd; }
    public void setUserPasswd(String userPasswd) { this.userPasswd = userPasswd; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public String getUserInfos() { return userInfos; }
    public void setUserInfos(String userInfos) { this.userInfos = userInfos; }
    
    public String getRegisterTime() { return registerTime; }
    public void setRegisterTime(String registerTime) { this.registerTime = registerTime; }
    
    public String getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(String lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    
    public String getLastLoginIpAddr() { return lastLoginIpAddr; }
    public void setLastLoginIpAddr(String lastLoginIpAddr) { this.lastLoginIpAddr = lastLoginIpAddr; }
    
    public Boolean getUserStatus() { return userStatus; }
    public void setUserStatus(Boolean userStatus) { this.userStatus = userStatus; }
    
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    
    public String getUserKey1() { return userKey1; }
    public void setUserKey1(String userKey1) { this.userKey1 = userKey1; }
    
    public String getUserKey2() { return userKey2; }
    public void setUserKey2(String userKey2) { this.userKey2 = userKey2; }
    
    public String getUserKey3() { return userKey3; }
    public void setUserKey3(String userKey3) { this.userKey3 = userKey3; }
    
    public String getUserKey4() { return userKey4; }
    public void setUserKey4(String userKey4) { this.userKey4 = userKey4; }
    
    public String getUserKey5() { return userKey5; }
    public void setUserKey5(String userKey5) { this.userKey5 = userKey5; }
    
    public String getUserKey6() { return userKey6; }
    public void setUserKey6(String userKey6) { this.userKey6 = userKey6; }
    
    // ==================== Builder模式 ====================
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 从当前对象创建Builder
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
        
        public UserDTO build() {
            return new UserDTO(this);
        }
    }
    
    public UserDTO trim() {
        return this.toBuilder()
                .userAccount(userAccount != null ? userAccount.trim().toLowerCase() : null)
                .nickname(nickname != null ? nickname.trim() : null)
                .userId(userId != null ? userId.trim() : null)
                .roleId(roleId != null ? roleId.trim() : null)
                .build();
    }
    
    // ==================== 静态工厂方法 ====================
    
    public static UserDTO forUpdate(String userId, String nickname, String userInfos) {
        return UserDTO.builder()
                .userId(userId)
                .nickname(nickname)
                .userInfos(userInfos)
                .build();
    }
    
    // ==================== Object方法重写 ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(userId, userDTO.userId) &&
               Objects.equals(userAccount, userDTO.userAccount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, userAccount);
    }
    
    @Override
    public String toString() {
        return "UserDTO{" +
               "userId='" + userId + '\'' +
               ", userAccount='" + userAccount + '\'' +
               ", userPasswd='[PROTECTED]'" +
               ", nickname='" + nickname + '\'' +
               ", userStatus=" + userStatus +
               ", roleId='" + roleId + '\'' +
               '}';
    }
}