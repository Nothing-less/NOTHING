package icu.nothingless.pojo.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import icu.nothingless.pojo.bean.Message;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.MSGEngine;

@JsonDeserialize(as = Message.class)
public interface iMSGAdapter extends iSTAdapter<iMSGAdapter> {
    @Override
    default Long save() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).save(this);
    }

    @Override
    default Long delete() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).delete(this);
    }

    @Override
    default List<iMSGAdapter> query() throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).query(this);
    }

    /********************** 业务相关接口 **********************/
    default Long saveMessage(Message msg) throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).saveMessage(msg);
    }

    default boolean markAsRead(Long userId, Long friendId) throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).markAsRead(userId, friendId);
    }

    default List<Message> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).getChatHistory(userId, friendId, lastMsgId, limit);
    }

    default List<Message> getUnreadMessages(Long userId) throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).getUnreadMessages(userId);
    }

    default boolean recallMessage(Long msgId, Long userId) throws Exception {
        return BaseEngine.getInstance(MSGEngine.class).recallMessage(msgId, userId);
    }

}
