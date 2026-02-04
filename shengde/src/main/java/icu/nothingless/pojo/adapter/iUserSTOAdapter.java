package icu.nothingless.pojo.adapter;

import java.util.List;

import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.UserSTOEngine;

public interface iUserSTOAdapter extends iSTAdapter<iUserSTOAdapter> {
    @Override
    default int save() {
        return BaseEngine.getInstance(UserSTOEngine.class).save(this);
    }

    @Override
    default int delete() {
        return BaseEngine.getInstance(UserSTOEngine.class).delete(this);
    }

    @Override
    default List<iUserSTOAdapter> query() {
        return BaseEngine.getInstance(UserSTOEngine.class).query(this);
    }

    public String getUserId();

    public void setUserId(String userId);

    public String getUserAccount();

    public void setUserAccount(String userAccount);

    public String getUserPasswd();

    public void setUserPasswd(String userPasswd);

    public String getNickname();

    public void setNickname(String nickname);

    public String getUserInfos();

    public void setUserInfos(String userInfos);

    public String getRegisterTime();

    public void setRegisterTime(String registerTime);

    public String getLastLoginTime();

    public void setLastLoginTime(String lastLoginTime);

    public String getLastLoginIpAddr();

    public void setLastLoginIpAddr(String lastLoginIpAddr);

    public Boolean getUserStatus();

    public void setUserStatus(Boolean userStatus);

    public String getRoleId();

    public void setRoleId(String roleId);

    public String getUserKey1();

    public void setUserKey1(String userKey1);

    public String getUserKey2();

    public void setUserKey2(String userKey2);

    public String getUserKey3();

    public void setUserKey3(String userKey3);

    public String getUserKey4();

    public void setUserKey4(String userKey4);

    public String getUserKey5();

    public void setUserKey5(String userKey5);

    public String getUserKey6();

    public void setUserKey6(String userKey6);

}
