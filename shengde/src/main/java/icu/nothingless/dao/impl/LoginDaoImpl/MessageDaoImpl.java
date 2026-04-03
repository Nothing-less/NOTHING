package icu.nothingless.dao.impl.LoginDaoImpl;

import java.util.List;

import icu.nothingless.dao.interfaces.iMessageDao;
import icu.nothingless.pojo.bean.Message;

public class MessageDaoImpl implements iMessageDao {
    
    // 保存消息
    @Override
    public Long saveMessage(Message msg) {
        return null;
    }
    
    // 标记消息为已读
    @Override
    public boolean markAsRead(Long userId, Long friendId) {
        return false;
    }
    
    // 获取聊天记录(分页)
    @Override
    public List<Message> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) {
        return null;
    }
    
    // 获取未读消息列表
    @Override
    public List<Message> getUnreadMessages(Long userId) {
        return null;
    }
    
    // 撤回消息
    @Override
    public boolean recallMessage(Long msgId, Long userId) {

        return false;
    }
}