package icu.nothingless.service.interfaces;

import java.util.List;

import icu.nothingless.pojo.bean.Message;

public interface iMessageService {

    // 发送消息
    Message sendMessage(Long senderId, Long receiverId, String content, Integer msgType);

    // 获取聊天记录
    List<Message> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit);

    // 获取未读消息(登录时拉取)
    List<Message> getUnreadMessages(Long userId);

    // 标记已读
    void markAsRead(Long userId, Long friendId);

    // 撤回消息
    boolean recallMessage(Long msgId, Long userId);

}