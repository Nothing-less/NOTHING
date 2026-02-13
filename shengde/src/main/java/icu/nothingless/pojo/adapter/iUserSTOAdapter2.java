package icu.nothingless.pojo.adapter;

/**
 * 用户模块接口 - 纯接口定义，无任何实现代码
 */
public interface iUserSTOAdapter2 extends icu.nothingless.pojo.adapter.iSTAdapter2<iUserSTOAdapter2> {
    
    // 业务字段定义
    String getUserId();
    void setUserId(String userId);
    
    String getUserAccount();
    void setUserAccount(String userAccount);
    
    String getUserPasswd();
    void setUserPasswd(String userPasswd);
    
    String getNickname();
    void setNickname(String nickname);
    
    String getUserInfos();
    void setUserInfos(String userInfos);
    
    String getRegisterTime();
    void setRegisterTime(String registerTime);
    
    String getLastLoginTime();
    void setLastLoginTime(String lastLoginTime);
    
    String getLastLoginIpAddr();
    void setLastLoginIpAddr(String lastLoginIpAddr);
    
    Boolean getUserStatus();
    void setUserStatus(Boolean userStatus);
    
    String getRoleId();
    void setRoleId(String roleId);
    
    String getUserKey1();
    void setUserKey1(String userKey1);
    
    String getUserKey2();
    void setUserKey2(String userKey2);
    
    String getUserKey3();
    void setUserKey3(String userKey3);
    
    String getUserKey4();
    void setUserKey4(String userKey4);
    
    String getUserKey5();
    void setUserKey5(String userKey5);
    
    String getUserKey6();
    void setUserKey6(String userKey6);
    
}