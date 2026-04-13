package icu.nothingless.pojo.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import icu.nothingless.pojo.bean.UserBean;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.UserEngine;
import icu.nothingless.pojo.ibean.IUserBean;

@JsonDeserialize(as = UserBean.class)
public interface IUserAdapter extends IAdapter<IUserAdapter>, IUserBean {

    
     // user status: true for active, false for inactive
     public static final Boolean STATUS_ACTIVE = true;
     public static final Boolean STATUS_INACTIVE = false;

     // userKey1 : "STATUS_ONLINE" or "STATUS_OFFLINE"
     public static final String STATUS_ONLINE = "ONLINE";
     public static final String STATUS_OFFLINE = "OFFLINE";


    @Override
    default Long save() throws Exception{
        return BaseEngine.getInstance(UserEngine.class).save(this);
    }

    @Override
    default Long delete() throws Exception{
        return BaseEngine.getInstance(UserEngine.class).delete(this);
    }

    @Override
    default List<IUserAdapter> query() throws Exception{
        return BaseEngine.getInstance(UserEngine.class).query(this);
    }

    @Override
    default Long update() throws Exception {
        return BaseEngine.getInstance(UserEngine.class).update(this);
    }

}
