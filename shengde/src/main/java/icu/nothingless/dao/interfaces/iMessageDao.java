package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.pojo.adapter.IMSGAdapter;

public interface IMessageDao<T extends IMSGAdapter> {

    // 保存消息
    Long saveMessage(T msg)throws Exception;

    // 标记消息为已读
    Boolean markAsRead(Long userId, Long friendId)throws Exception;

    // 获取聊天记录(分页)
    List<T> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit)throws Exception;

    // 获取未读消息列表
    List<T> getUnreadMessages(Long userId)throws Exception;

    // 撤回消息
    Boolean recallMessage(Long msgId, Long userId)throws Exception;

}