package icu.nothingless.pojo.bean;

import icu.nothingless.pojo.adapter.iUserSTOAdapter;

public record UserSTO(

     String userId, /* Primary Key */
     String account, /* user login account */
     String pwdString, /* password */
     String nickname, /* user nickname */
     String infos, /* user additional information */
     String registerTime, /* registration DateTime */
     String lastLoginTime, /* last login DateTime */
     String lastLoginIpAddr, /* last login IP address */
     Boolean status, /* True for active, False for inactive */
     String roleId, /* user role ID */
     String userKey1, /* Alternate fields 1~6 */
     String userKey2,
     String userKey3,
     String userKey4,
     String userKey5,
     String userKey6
) implements iUserSTOAdapter {
     

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
}
