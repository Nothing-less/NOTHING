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

}
