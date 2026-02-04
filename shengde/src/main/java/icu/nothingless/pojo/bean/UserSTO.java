package icu.nothingless.pojo.bean;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;

public class UserSTO implements iUserSTOAdapter {

     private String userId; /* Primary Key */
     private String userAccount; /* user login account */
     private String userPasswd; /* password */
     private String nickname; /* user nickname */
     private String userInfos; /* user additional information */
     private String registerTime; /* registration DateTime */
     private String lastLoginTime; /* last login DateTime */
     private String lastLoginIpAddr; /* last login IP address */
     private Boolean userStatus; /* True for active; False for inactive */
     private String roleId; /* user role ID */
     private String userKey1; /* Alternate fields 1~6 */
     private String userKey2;
     private String userKey3;
     private String userKey4;
     private String userKey5;
     private String userKey6;

     @Override
     public String getUserId() {
          return userId;
     }

     @Override
     public void setUserId(String userId) {
          this.userId = userId;
     }

     @Override
     public String getUserAccount() {
          return userAccount;
     }

     @Override
     public void setUserAccount(String userAccount) {
          this.userAccount = userAccount;
     }

     @Override
     public String getUserPasswd() {
          return userPasswd;
     }

     @Override
     public void setUserPasswd(String userPasswd) {
          this.userPasswd = userPasswd;
     }

     @Override
     public String getNickname() {
          return nickname;
     }

     @Override
     public void setNickname(String nickname) {
          this.nickname = nickname;
     }

     @Override
     public String getUserInfos() {
          return userInfos;
     }

     @Override
     public void setUserInfos(String userInfos) {
          this.userInfos = userInfos;
     }

     @Override
     public String getRegisterTime() {
          return registerTime;
     }

     @Override
     public void setRegisterTime(String registerTime) {
          this.registerTime = registerTime;
     }

     @Override
     public String getLastLoginTime() {
          return lastLoginTime;
     }

     @Override
     public void setLastLoginTime(String lastLoginTime) {
          this.lastLoginTime = lastLoginTime;
     }

     @Override
     public String getLastLoginIpAddr() {
          return lastLoginIpAddr;
     }

     @Override
     public void setLastLoginIpAddr(String lastLoginIpAddr) {
          this.lastLoginIpAddr = lastLoginIpAddr;
     }

     @Override
     public Boolean getUserStatus() {
          return userStatus;
     }

     @Override
     public void setUserStatus(Boolean userStatus) {
          this.userStatus = userStatus;
     }

     @Override
     public String getRoleId() {
          return roleId;
     }

     @Override
     public void setRoleId(String roleId) {
          this.roleId = roleId;
     }

     @Override
     public String getUserKey1() {
          return userKey1;
     }

     @Override
     public void setUserKey1(String userKey1) {
          this.userKey1 = userKey1;
     }

     @Override
     public String getUserKey2() {
          return userKey2;
     }

     @Override
     public void setUserKey2(String userKey2) {
          this.userKey2 = userKey2;
     }

     @Override
     public String getUserKey3() {
          return userKey3;
     }

     @Override
     public void setUserKey3(String userKey3) {
          this.userKey3 = userKey3;
     }

     @Override
     public String getUserKey4() {
          return userKey4;
     }

     @Override
     public void setUserKey4(String userKey4) {
          this.userKey4 = userKey4;
     }

     @Override
     public String getUserKey5() {
          return userKey5;
     }

     @Override
     public void setUserKey5(String userKey5) {
          this.userKey5 = userKey5;
     }

     @Override
     public String getUserKey6() {
          return userKey6;
     }

     @Override
     public void setUserKey6(String userKey6) {
          this.userKey6 = userKey6;
     }

}
