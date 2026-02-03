package icu.nothingless.pojo.bean;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;

public class UserSTO implements iUserSTOAdapter {

    private String userId; /* Primary Key */
    private String account; /* user login account */
    private String pwdString; /* password */
    private String nickname; /* user nickname */
    private String infos; /* user additional information */
    private String registerTime; /* registration DateTime */
    private String lastLoginTime; /* last login DateTime */
    private String lastLoginIpAddr; /* last login IP address */
    private Boolean status; /* True for active; False for inactive */
    private String roleId; /* user role ID */
    private String userKey1; /* Alternate fields 1~6 */
    private String userKey2;
    private String userKey3;
    private String userKey4;
    private String userKey5;
    private String userKey6;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwdString() {
        return pwdString;
    }

    public void setPwdString(String pwdString) {
        this.pwdString = pwdString;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getInfos() {
        return infos;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIpAddr() {
        return lastLoginIpAddr;
    }

    public void setLastLoginIpAddr(String lastLoginIpAddr) {
        this.lastLoginIpAddr = lastLoginIpAddr;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getUserKey1() {
        return userKey1;
    }

    public void setUserKey1(String userKey1) {
        this.userKey1 = userKey1;
    }

    public String getUserKey2() {
        return userKey2;
    }

    public void setUserKey2(String userKey2) {
        this.userKey2 = userKey2;
    }

    public String getUserKey3() {
        return userKey3;
    }

    public void setUserKey3(String userKey3) {
        this.userKey3 = userKey3;
    }

    public String getUserKey4() {
        return userKey4;
    }

    public void setUserKey4(String userKey4) {
        this.userKey4 = userKey4;
    }

    public String getUserKey5() {
        return userKey5;
    }

    public void setUserKey5(String userKey5) {
        this.userKey5 = userKey5;
    }

    public String getUserKey6() {
        return userKey6;
    }

    public void setUserKey6(String userKey6) {
        this.userKey6 = userKey6;
    }

}
