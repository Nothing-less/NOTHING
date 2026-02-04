package icu.nothingless.pojo.adapter;

import java.util.List;

import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.UserSTOEngine;

public interface iUserSTOAdapter extends iSTAdapter<iUserSTOAdapter> {
    @Override
    default int save() {
        BaseEngine.getInstance(UserSTOEngine.class).save(this);
        return 0;
    }

    @Override
    default int delete() {
        BaseEngine.getInstance(UserSTOEngine.class).delete(this);
        return 0;
    }

    @Override
    default List<iUserSTOAdapter> query() {
        return BaseEngine.getInstance(UserSTOEngine.class).query(this);
    }

    public String getUserId();

    public String getAccount();

    public String getPwdString();

    public String getNickname();

    public String getInfos();

    public String getRegisterTime();

    public String getLastLoginTime();

    public String getLastLoginIpAddr();

    public Boolean getStatus();

    public String getRoleId();

    public String getUserKey1();

    public String getUserKey2();

    public String getUserKey3();

    public String getUserKey4();

    public String getUserKey5();

    public String getUserKey6();

    public void setUserId(String userId);

    public void setAccount(String account);

    public void setPwdString(String pwdString);

    public void setNickname(String nickname);

    public void setInfos(String infos);

    public void setRegisterTime(String registerTime);

    public void setLastLoginTime(String lastLoginTime);

    public void setLastLoginIpAddr(String lastLoginIpAddr);

    public void setStatus(Boolean status);

    public void setRoleId(String roleId);

    public void setUserKey1(String userKey1);

    public void setUserKey2(String userKey2);

    public void setUserKey3(String userKey3);

    public void setUserKey4(String userKey4);

    public void setUserKey5(String userKey5);

    public void setUserKey6(String userKey6);

}
