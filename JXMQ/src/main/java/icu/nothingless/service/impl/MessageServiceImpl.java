package icu.nothingless.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.R;
import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.IMessageDao;
import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.pojo.dto.Message;
import icu.nothingless.service.interfaces.IMessageService;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ServiceFactory;

@SuppressWarnings("unchecked")
public class MessageServiceImpl implements IMessageService<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private static final IMessageDao<Message> messageDao = (IMessageDao<Message>) ServiceFactory
            .getSingleton(IMessageDao.class);

    // 发送消息
    @Override
    public RespEntity<Message> sendMessage(Long senderId, Long receiverId, String content, Integer msgType) {
        if (senderId == null || receiverId == null) {
            return RespEntity.badRequest("Sender id and receiver id are required");
        }
        if (content == null || content.isBlank()) {
            return RespEntity.badRequest("Message content is required");
        }

        icu.nothingless.pojo.bean.MessageBean msgBean = new MessageBean();
        msgBean.setSenderId(senderId);
        msgBean.setReceiverId(receiverId);
        msgBean.setMsgType(msgType == null ? Message.TYPE_TEXT : msgType);
        msgBean.setContents(content);
        msgBean.setMsgStatus(MessageBean.STATUS_UNREAD);

        try {
            R<Long> saveResult = messageDao.saveMessage(Message.fromEntity(msgBean));
            if (!saveResult.isSuccess() || saveResult.data() == null) {
                return RespEntity.error("Send message failed");
            }

            msgBean.setMsgId(saveResult.data());
            Message message = Message.builder().from(msgBean).withCurrentUser(senderId).build();

            Integer receiverStatus = ChatJedisUtil.getUserStatus(String.valueOf(receiverId));
            if (receiverStatus != null && receiverStatus == 1) {
                ChatJedisUtil.incrUnread(receiverId, senderId);
                ChatJedisUtil.cacheRecentMessage(receiverId, senderId, msgBean);
            } else {
                ChatJedisUtil.pushOfflineMessage(receiverId, msgBean);
            }
            ChatJedisUtil.cacheRecentMessage(senderId, receiverId, msgBean);
            return RespEntity.success(message);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <sendMessage>: ", e);
            return RespEntity.error("Send message failed");
        }
    }

    // 获取聊天记录
    @Override
    public RespEntity<List<Message>> getChatHistory(Long userId, Long friendId, Long lastMsgId, int limit) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        try {
            R<List<Message>> ret = messageDao.getChatHistory(userId, friendId, lastMsgId, limit);
            if (!ret.isSuccess()) {
                return RespEntity.error("Get chat history failed");
            }
            return RespEntity.success((List<Message>)(ret.data()));
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getChatHistory>: ", e);
            return RespEntity.error("Get chat history failed");
        }
    }

    // 获取未读消息(登录时拉取)
    @Override
    public RespEntity<List<Message>> getUnreadMessages(Long userId) {
        if (userId == null) {
            return RespEntity.badRequest("User id is required");
        }
        try {
            R<List<Message>> ret = messageDao.getUnreadMessages(userId);
            List<Message> messages = (List<Message>)ret.data();

            List<MessageBean> offlineMsgs = ChatJedisUtil.popOfflineMessages(userId);
            if (offlineMsgs != null && !offlineMsgs.isEmpty()) {
                List<Message> offlineMessages = convertMessages(offlineMsgs, userId);
                offlineMessages.addAll(messages);
                messages = offlineMessages;
            }
            return RespEntity.success(messages);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getUnreadMessages>: ", e);
            return RespEntity.error("Get unread messages failed");
        }
    }

    // 标记已读
    @Override
    public RespEntity<Void> markAsRead(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        try {
            R<Boolean> ret = messageDao.markAsRead(userId, friendId);
            if (!ret.isSuccess() || !Boolean.TRUE.equals(ret.data())) {
                return RespEntity.error("Mark as read failed");
            }
            ChatJedisUtil.clearUnread(userId, friendId);
            return RespEntity.success("Messages marked as read", null);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <markAsRead>: ", e);
            return RespEntity.error("Mark as read failed");
        }
    }

    // 撤回消息
    @Override
    public RespEntity<Void> recallMessage(Long msgId, Long userId) {
        if (msgId == null || userId == null) {
            return RespEntity.badRequest("Message id and user id are required");
        }
        try {
            R<Boolean> ret = messageDao.recallMessage(msgId, userId);
            if (!ret.isSuccess() || !Boolean.TRUE.equals(ret.data())) {
                return RespEntity.error("Recall message failed");
            }
            return RespEntity.success("Message recalled", null);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <recallMessage>: ", e);
            return RespEntity.error("Recall message failed");
        }
    }

    private List<Message> convertMessages(List<icu.nothingless.pojo.bean.MessageBean> beans, Long currentUserId) {
        List<Message> messages = new ArrayList<>();
        if (beans == null || beans.isEmpty()) {
            return messages;
        }
        for (MessageBean bean : beans) {
            messages.add(Message.fromEntity(bean, currentUserId));
        }
        return messages;
    }
}
