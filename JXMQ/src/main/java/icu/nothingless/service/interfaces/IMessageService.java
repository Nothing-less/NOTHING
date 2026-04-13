package icu.nothingless.service.interfaces;

import java.util.List;

import icu.nothingless.pojo.bean.MessageBean;

public interface IMessageService {

    // 发送消息
    MessageBean sendMessage(Long senderId, Long receiverId, String content, Integer msgType);

    // 获取聊天记录
    List<MessageBean> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit);

    // 获取未读消息(登录时拉取)
    List<MessageBean> getUnreadMessages(Long userId);

    // 标记已读
    void markAsRead(Long userId, Long friendId);

    // 撤回消息
    boolean recallMessage(Long msgId, Long userId);

}