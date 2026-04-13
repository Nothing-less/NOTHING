package icu.nothingless.pojo.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import icu.nothingless.pojo.bean.FriendshipBean;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.FSEngine;
import icu.nothingless.pojo.ibean.IFSBean;

@JsonDeserialize(as = FriendshipBean.class)
public interface IFSAdapter extends IAdapter<IFSAdapter>, IFSBean{

    public static final int STATUS_PENDING = 0;   // 待确认
    public static final int STATUS_AGREED = 1;    // 已同意
    public static final int STATUS_REJECTED = 2;  // 已拒绝
    public static final int STATUS_DELETED = 3;   // 已删除

    @Override
    default Long save() throws Exception{
        return BaseEngine.getInstance(FSEngine.class).save(this);
    }

    @Override
    default Long delete() throws Exception{
        return BaseEngine.getInstance(FSEngine.class).delete(this);
    }

    @Override
    default List<IFSAdapter> query() throws Exception{
        return BaseEngine.getInstance(FSEngine.class).query(this);
    }

    @Override
    default Long update() throws Exception {
        return BaseEngine.getInstance(FSEngine.class).update(this);
    }
    
}
