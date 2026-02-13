package icu.nothingless.pojo.bean;

import java.util.List;

import icu.nothingless.pojo.adapter.iUserSTOAdapter2;
import icu.nothingless.pojo.core.QueryCondition;

public class UserSTO2 implements iUserSTOAdapter2{

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

     @Override
     public String toString() {
          return "UserSTO{" +
                    "userId='" + userId + '\'' +
                    ", userAccount='" + userAccount + '\'' +
                    ", userPasswd='" + userPasswd + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", userInfos='" + userInfos + '\'' +
                    ", registerTime='" + registerTime + '\'' +
                    ", lastLoginTime='" + lastLoginTime + '\'' +
                    ", lastLoginIpAddr='" + lastLoginIpAddr + '\'' +
                    ", userStatus=" + userStatus +
                    ", roleId='" + roleId + '\'' +
                    ", userKey1='" + userKey1 + '\'' +
                    ", userKey2='" + userKey2 + '\'' +
                    ", userKey3='" + userKey3 + '\'' +
                    ", userKey4='" + userKey4 + '\'' +
                    ", userKey5='" + userKey5 + '\'' +
                    ", userKey6='" + userKey6 + '\'' +
                    '}';
     }

     @Override
     public Class<iUserSTOAdapter2> getAdapterClass() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'getAdapterClass'");
     }

     @Override
     public Long save() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'save'");
     }

     @Override
     public Long delete() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'delete'");
     }

     @Override
     public Long deletePhysical() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'deletePhysical'");
     }

     @Override
     public List<iUserSTOAdapter2> query() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'query'");
     }

     @Override
     public iUserSTOAdapter2 queryOne() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'queryOne'");
     }

     @Override
     public iUserSTOAdapter2 queryById(Object id) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'queryById'");
     }

     @Override
     public List<iUserSTOAdapter2> queryAll() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'queryAll'");
     }

     @Override
     public iUserSTOAdapter2 eq(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'eq'");
     }

     @Override
     public iUserSTOAdapter2 neq(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'neq'");
     }

     @Override
     public iUserSTOAdapter2 gt(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'gt'");
     }

     @Override
     public iUserSTOAdapter2 gte(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'gte'");
     }

     @Override
     public iUserSTOAdapter2 lt(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'lt'");
     }

     @Override
     public iUserSTOAdapter2 lte(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'lte'");
     }

     @Override
     public iUserSTOAdapter2 like(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'like'");
     }

     @Override
     public iUserSTOAdapter2 leftLike(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'leftLike'");
     }

     @Override
     public iUserSTOAdapter2 rightLike(String field, Object value) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'rightLike'");
     }

     @Override
     public iUserSTOAdapter2 in(String field, List<?> values) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'in'");
     }

     @Override
     public iUserSTOAdapter2 isNull(String field) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'isNull'");
     }

     @Override
     public iUserSTOAdapter2 isNotNull(String field) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'isNotNull'");
     }

     @Override
     public iUserSTOAdapter2 or() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'or'");
     }

     @Override
     public iUserSTOAdapter2 orderBy(String field, boolean asc) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'orderBy'");
     }

     @Override
     public iUserSTOAdapter2 limit(int limit) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'limit'");
     }

     @Override
     public iUserSTOAdapter2 offset(int offset) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'offset'");
     }

     @Override
     public List<iUserSTOAdapter2> execute() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'execute'");
     }

     @Override
     public iUserSTOAdapter2 executeOne() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'executeOne'");
     }

     @Override
     public QueryCondition getQueryCondition() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'getQueryCondition'");
     }

     @Override
     public void clearCondition() {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'clearCondition'");
     }
}
