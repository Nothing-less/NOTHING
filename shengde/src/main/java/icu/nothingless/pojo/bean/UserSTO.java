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

     @Override
     public String getUserId() {
          return this.userId;
     }

     @Override
     public String getAccount() {
          return this.account;
     }

     @Override
     public String getPwdString() {
          return this.pwdString;
     }

     @Override
     public String getNickname() {
          return this.nickname;
     }

     @Override
     public String getInfos() {
          return this.infos;
     }

     @Override
     public String getRegisterTime() {
          return this.registerTime;
     }

     @Override
     public String getLastLoginTime() {
          return this.lastLoginTime;
     }

     @Override
     public String getLastLoginIpAddr() {
          return this.lastLoginIpAddr;
     }

     @Override
     public Boolean getStatus() {
          return this.status;
     }

     @Override
     public String getRoleId() {
          return this.roleId;
     }

     @Override
     public String getUserKey1() {
          return this.userKey1;
     }

     @Override
     public String getUserKey2() {
          return this.userKey2;
     }

     @Override
     public String getUserKey3() {
          return this.userKey3;
     }

     @Override
     public String getUserKey4() {
          return this.userKey4;
     }

     @Override
     public String getUserKey5() {
          return this.userKey5;
     }

     @Override
     public String getUserKey6() {
          return this.userKey6;
     }

     @Override
     public void setUserId(String userId) {
          this.userId = userId;
     }

     @Override
     public void setAccount(String account) {
          this.account = account;
     }

     @Override
     public void setPwdString(String pwdString) {
          this.pwdString = pwdString;
     }

     @Override
     public void setNickname(String nickname) {
          this.nickname = nickname;
     }

     @Override
     public void setInfos(String infos) {
          this.infos = infos;
     }

     @Override
     public void setRegisterTime(String registerTime) {
          this.registerTime = registerTime;
     }

     @Override
     public void setLastLoginTime(String lastLoginTime) {
          this.lastLoginTime = lastLoginTime;
     }

     @Override
     public void setLastLoginIpAddr(String lastLoginIpAddr) {
          this.lastLoginIpAddr = lastLoginIpAddr;
     }

     @Override
     public void setStatus(Boolean status) {
          this.status = status;
     }

     @Override
     public void setRoleId(String roleId) {
          this.roleId = roleId;
     }

     @Override
     public void setUserKey1(String userKey1) {
          this.userKey1 = userKey1;
     }

     @Override
     public void setUserKey2(String userKey2) {
          this.userKey2 = userKey2;
     }

     @Override
     public void setUserKey3(String userKey3) {
          this.userKey3 = userKey3;
     }

     @Override
     public void setUserKey4(String userKey4) {
          this.userKey4 = userKey4;
          throw new UnsupportedOperationException("Unimplemented method 'setUserKey4'");
     }

     @Override
     public void setUserKey5(String userKey5) {
          this.userKey5 = userKey5;
     }

     @Override
     public void setUserKey6(String userKey6) {
          this.userKey6 = userKey6;
     }
}
