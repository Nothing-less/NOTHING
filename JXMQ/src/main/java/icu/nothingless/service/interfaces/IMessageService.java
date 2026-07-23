package icu.nothingless.service.interfaces;

import java.util.List;

import icu.nothingless.commons.RespEntity;


public interface IMessageService<T> {

    // 发送消息
    RespEntity<T> sendMessage(Long senderId, Long receiverId, String content, Integer msgType);

    // 获取聊天记录
    RespEntity<List<T>> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit);

    // 获取未读消息(登录时拉取)
    RespEntity<List<T>> getUnreadMessages(Long userId);

    // 标记已读
    RespEntity<Void> markAsRead(Long userId, Long friendId);

    // 撤回消息
    RespEntity<Void> recallMessage(Long msgId, Long userId);

}