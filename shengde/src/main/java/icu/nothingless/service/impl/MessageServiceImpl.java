package icu.nothingless.service.impl;

import java.util.List;

import icu.nothingless.dao.interfaces.iMessageDao;
import icu.nothingless.pojo.bean.Message;
import icu.nothingless.service.interfaces.iMessageService;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ServiceFactory;

public class MessageServiceImpl implements iMessageService {
    private iMessageDao messageDao = ServiceFactory.getSingleton(iMessageDao.class);
    
    // 发送消息
    @Override
    public Message sendMessage(Long senderId, Long receiverId, String content, Integer msgType) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setMsgType(msgType);
        msg.setContent(content);
        msg.setStatus(Message.STATUS_UNREAD);
        
        Long msgId = messageDao.saveMessage(msg);
        if (msgId != null) {
            msg.setMsgId(msgId);
            
            // 检查接收者是否在线
            Integer receiverStatus = ChatJedisUtil.getUserStatus("" + receiverId);
            
            if (receiverStatus == 1) {
                // 在线：增加未读计数
                ChatJedisUtil.incrUnread(receiverId, senderId);
                // 缓存到最近消息
                ChatJedisUtil.cacheRecentMessage(receiverId, senderId, msg);
            } else {
                // 离线：存入离线队列
                ChatJedisUtil.pushOfflineMessage(receiverId, msg);
            }
            
            // 同时缓存到发送者的最近消息
            ChatJedisUtil.cacheRecentMessage(senderId, receiverId, msg);
            
            return msg;
        }
        return null;
    }
    
    // 获取聊天记录
    @Override
    public List<Message> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) {
        return messageDao.getChatHistory(userId, friendId, lastMsgId, limit);
    }
    
    // 获取未读消息(登录时拉取)
    @Override
    public List<Message> getUnreadMessages(Long userId) {
        List<Message> list = messageDao.getUnreadMessages(userId);
        
        // 同时获取Redis中的离线消息
        List<Message> offlineMsgs = ChatJedisUtil.popOfflineMessages(userId);
        if (!offlineMsgs.isEmpty()) {
            list.addAll(0, offlineMsgs);
        }
        
        return list;
    }
    
    // 标记已读
    @Override
    public void markAsRead(Long userId, Long friendId) {
        messageDao.markAsRead(userId, friendId);
        ChatJedisUtil.clearUnread(userId, friendId);
    }
    
    // 撤回消息
    @Override
    public boolean recallMessage(Long msgId, Long userId) {
        return messageDao.recallMessage(msgId, userId);
    }
}