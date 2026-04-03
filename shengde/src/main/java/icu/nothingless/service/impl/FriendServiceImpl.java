package icu.nothingless.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.iFriendDao;
import icu.nothingless.dto.UserDTO;
import icu.nothingless.pojo.bean.Friendship;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.service.interfaces.iFriendService;
import icu.nothingless.service.interfaces.iUserService;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ServiceFactory;

public class FriendServiceImpl implements iFriendService {
    private static final iUserService<UserDTO> userService = (iUserService<UserDTO>) ServiceFactory
            .getSingleton(iUserService.class);
    private static final iFriendDao friendDao = (iFriendDao) ServiceFactory.getSingleton(iFriendDao.class);
    private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);

    // 搜索用户
    @Override
    public List<UserDTO> searchUsers(Long userId, String keyword) {
        UserDTO searchTarget = new UserDTO();
        searchTarget.setUserId("" + userId);
        searchTarget.setNickname(keyword);
        searchTarget.setUserAccount(keyword);
        RespEntity<List<UserDTO>> result = userService.doSearch(searchTarget);
        if (result.isSuccess()) {
            return result.getData();
        }
        return null;
    }

    // 申请添加好友
    @Override
    public boolean applyFriend(Long userId, Long friendId, String applyMsg) {
        try {
            // 不能加自己
            if (userId.equals(friendId))
                return false;

            // 检查是否已经是好友
            if (friendDao.isFriend(userId, friendId))
                return false;

            // 检查是否已有待处理申请
            List<Friendship> exist = friendDao.getFriendship(userId, friendId);
            if (exist != null && !exist.isEmpty() && exist.get(0).getFsStatus() == Friendship.STATUS_PENDING) {
                return false;
            }

            boolean success = friendDao.applyFriend(userId, friendId, applyMsg);
            if (success) {
                // Redis通知对方有新的好友申请
                ChatJedisUtil.notifyFriendRequest(friendId, userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error occurred while executing function <applyFriend>: ", e);
        }
        return false;
    }

    // 获取好友列表
    @Override
    public List<Friendship> getFriendList(Long userId, String group, String keyword) {
        List<Friendship> list = new ArrayList<>();
        try {
            list = friendDao.getFriendList(userId, group, keyword);

            // 补充在线状态和未读消息数
            for (Friendship f : list) {
                var friend = f.getFriendInfo();
                // 从Redis获取实时在线状态
                Integer status = ChatJedisUtil.getUserStatus(friend.getUserId());
                friend.setUserKey1(status > 0 ? UserSTO.STATUS_ONLINE : UserSTO.STATUS_OFFLINE);

                // 获取未读消息数
                Long unread = ChatJedisUtil.getUnreadCount(userId, Long.parseLong(friend.getUserId()));
                f.setUnreadMsgCount(unread);
            }
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getFriendList>: ", e);
        }
        return list;
    }

    // 获取好友申请列表
    @Override
    public List<Friendship> getPendingRequests(Long userId) {
        try {
            return friendDao.getPendingRequests(userId);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getPendingRequests>: ", e);
        }
        return new ArrayList<>();
    }

    // 同意好友申请
    @Override
    public boolean agreeFriend(Long userId, Long friendId, String remark, String groupName) {
        boolean success = false;
        try {
            success = friendDao.agreeFriend(userId, friendId, remark, groupName);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <agreeFriend>: ", e);
        }
        if (success) {
            ChatJedisUtil.clearFriendRequests(userId, friendId);
        }
        return success;
    }

    // 拒绝好友申请
    @Override
    public boolean rejectFriend(Long userId, Long friendId) {
        try {
            return friendDao.rejectFriend(userId, friendId);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <rejectFriend>: ", e);
        }
        return false;
    }

    // 删除好友
    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        try {
            return friendDao.deleteFriend(userId, friendId);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <deleteFriend>: ", e);
        }
        return false;
    }

    // 修改好友信息
    @Override
    public boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName) {
        try {
            return friendDao.updateFriendInfo(userId, friendId, remark, groupName);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <updateFriendInfo>: ", e);
        }
        return false;
    }

    // 获取分组列表
    @Override
    public List<String> getGroups(Long userId) {
        try {
            return friendDao.getGroups(userId);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getGroups>: ", e);
        }
        return new ArrayList<>();
    }
}