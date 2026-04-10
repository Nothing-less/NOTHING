package icu.nothingless.pojo.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.MSGEngine;
import icu.nothingless.pojo.ibean.IMessageBean;

@JsonDeserialize(as = MessageBean.class)
public interface IMSGAdapter extends IAdapter<IMSGAdapter>, IMessageBean {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_FILE = 3;
    
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;
    public static final int STATUS_RECALLED = 2;


    @Override
    default Long save() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).save(this);
    }

    @Override
    default Long delete() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).delete(this);
    }

    @Override
    default List<IMSGAdapter> query() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).query(this);
    }
        @Override
    default Long update() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).update(this);
    }

}
