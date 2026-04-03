package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.pojo.bean.Message;

public interface iMessageDao {

    // 保存消息
    Long saveMessage(Message msg);

    // 标记消息为已读
    boolean markAsRead(Long userId, Long friendId);

    // 获取聊天记录(分页)
    List<Message> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit);

    // 获取未读消息列表
    List<Message> getUnreadMessages(Long userId);

    // 撤回消息
    boolean recallMessage(Long msgId, Long userId);

}