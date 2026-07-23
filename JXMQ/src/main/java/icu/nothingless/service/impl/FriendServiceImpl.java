package icu.nothingless.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.commons.R;
import icu.nothingless.commons.RespEntity;
import icu.nothingless.dao.interfaces.IFriendDao;
import icu.nothingless.pojo.dto.Friendship;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IFriendService;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ServiceFactory;

@SuppressWarnings("unchecked")
public class FriendServiceImpl implements IFriendService<Friendship, User> {
    private static final IUserService<User> userService = (IUserService<User>) ServiceFactory
            .getSingleton(IUserService.class);
    private static final IFriendDao<Friendship> friendDao = (IFriendDao<Friendship>) ServiceFactory
            .getSingleton(IFriendDao.class);
    private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);

    // 搜索用户
    @Override
    public RespEntity<List<User>> searchUsers(Long userId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return RespEntity.badRequest("Search keyword is required");
        }
        RespEntity<List<User>> result = userService.doSearch(keyword);
        if (result != null && result.isSuccess()) {
            return result;
        }
        return RespEntity.notFound("No results");
    }

    // 申请添加好友
    @Override
    public RespEntity<Void> applyFriend(Long userId, Long friendId, String applyMsg) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        if (userId.equals(friendId)) {
            return RespEntity.badRequest("Cannot add yourself as a friend");
        }

        try {
            R<Boolean> relation = friendDao.isFriend(userId, friendId);
            if (!relation.isSuccess()) {
                return RespEntity.error("Failed to check friendship status");
            }
            if (relation.isSuccess() && relation.code() == 2) {
                return RespEntity.error(RespEntity.CONFLICT, "You are already friends");
            }

            R<List<Friendship>> friendshipResult = friendDao.getFriendship(userId, friendId);
            if (!friendshipResult.isSuccess()) {
                return RespEntity.error("Failed to check existing friend requests");
            }
            List<Friendship> exist = friendshipResult.data();
            if (exist != null && !exist.isEmpty()) {
                Friendship first = exist.get(0);
                if (first.getFsStatus() != null && first.getFsStatus().equals(Friendship.STATUS_PENDING)) {
                    return RespEntity.error(RespEntity.CONFLICT, "A pending request already exists");
                }
            }

            R<Boolean> applyResult = friendDao.applyFriend(userId, friendId, applyMsg);
            if (applyResult.isSuccess() && Boolean.TRUE.equals(applyResult.data())) {
                ChatJedisUtil.notifyFriendRequest(friendId, userId);
                return RespEntity.success("Friend request sent successfully", null);
            }
            return RespEntity.error("Failed to submit friend request");
        } catch (Exception e) {
            logger.error("Error occurred while executing function <applyFriend>: ", e);
            return RespEntity.error("Apply failed");
        }
    }

    // 获取好友列表
    @Override
    public RespEntity<List<Friendship>> getFriendList(Long userId, String group, String keyword) {
        if (userId == null) {
            return RespEntity.badRequest("User id is required");
        }

        try {
            R<List<Friendship>> ret = friendDao.getFriendList(userId, group, keyword);
            if (!ret.isSuccess()) {
                return RespEntity.error("Get friends failed");
            }

            List<Friendship> list = ret.data();
            List<Friendship> returnList = new ArrayList<>();
            if (list == null || list.isEmpty()) {
                return RespEntity.success(new ArrayList<>());
            }

            for (Friendship f : list) {
                User friend = f.getFriendInfo();
                if (friend == null) {
                    logger.warn("FriendInfo is null for friendship: {}", f.getFsId());
                    continue;
                }

                Integer status = ChatJedisUtil.getUserStatus(friend.userId());
                User _friend = User.forUpdateOnlineStatus(friend, (status != null && status > 0));
                f.setFriendInfo(_friend);
                try {
                    if (_friend.userId() != null) {
                        Long friendIdValue = Long.parseLong(_friend.userId());
                        f.setUnreadMsgCount(ChatJedisUtil.getUnreadCount(userId, friendIdValue));
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn("Invalid friend userId format: {}", _friend.userId());
                }
                returnList.add(f);
            }
            return RespEntity.success(returnList);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getFriendList>: ", e);
            return RespEntity.error("Get friend list failed");
        }
    }

    // 获取好友申请列表
    @Override
    public RespEntity<List<Friendship>> getPendingRequests(Long userId) {
        if (userId == null) {
            return RespEntity.badRequest("User id is required");
        }
        try {
            R<List<Friendship>> ret = friendDao.getPendingRequests(userId);
            if (!ret.isSuccess()) {
                return RespEntity.error("Get pending requests failed");
            }
            List<Friendship> pending = ret.data();
            if (pending == null) {
                pending = new ArrayList<>();
            }
            return RespEntity.success(pending);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getPendingRequests>: ", e);
            return RespEntity.error("Get pending requests failed");
        }
    }

    // 同意好友申请
    @Override
    public RespEntity<Void> agreeFriend(Long userId, Long friendId, String remark, String groupName) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        try {
            ChatJedisUtil.clearFriendRequests(userId, friendId);
            R<Boolean> ret = friendDao.agreeFriend(userId, friendId, remark, groupName);
            if (!ret.isSuccess() || !Boolean.TRUE.equals(ret.data())) {
                ChatJedisUtil.restoreFriendRequests(userId, friendId);
                return RespEntity.error("Agree friend failed");
            }
            return RespEntity.success("Friend request accepted", null);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <agreeFriend>: ", e);
            ChatJedisUtil.restoreFriendRequests(userId, friendId);
            return RespEntity.error("Agree friend failed");
        }
    }

    // 拒绝好友申请
    @Override
    public RespEntity<Void> rejectFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        try {
            R<Boolean> ret = friendDao.rejectFriend(userId, friendId);
            if (!ret.isSuccess() || !Boolean.TRUE.equals(ret.data())) {
                return RespEntity.error("Reject friend failed");
            }
            return RespEntity.success("Friend request rejected", null);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <rejectFriend>: ", e);
            return RespEntity.error("Reject friend failed");
        }
    }

    // 删除好友
    @Override
    public RespEntity<Void> deleteFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        try {
            R<Boolean> ret = friendDao.deleteFriend(userId, friendId);
            if (!ret.isSuccess() || !Boolean.TRUE.equals(ret.data())) {
                return RespEntity.error("Delete friend failed");
            }
            return RespEntity.success("Friend deleted", null);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <deleteFriend>: ", e);
            return RespEntity.error("Delete friend failed");
        }
    }

    // 修改好友信息
    @Override
    public RespEntity<Void> updateFriendInfo(Long userId, Long friendId, String remark, String groupName) {
        if (userId == null || friendId == null) {
            return RespEntity.badRequest("User id and friend id are required");
        }
        try {
            R<Boolean> ret = friendDao.updateFriendInfo(userId, friendId, remark, groupName);
            if (!ret.isSuccess() || !Boolean.TRUE.equals(ret.data())) {
                return RespEntity.error("Update friend info failed");
            }
            return RespEntity.success("Friend info updated", null);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <updateFriendInfo>: ", e);
            return RespEntity.error("Update friend info failed");
        }
    }

    // 获取分组列表
    @Override
    public RespEntity<List<String>> getGroups(Long userId) {
        if (userId == null) {
            return RespEntity.badRequest("User id is required");
        }
        try {
            R<List<String>> ret = friendDao.getGroups(userId);
            if (!ret.isSuccess()) {
                return RespEntity.error("Get groups failed");
            }
            List<String> groups = ret.data();
            if (groups == null || groups.isEmpty()) {
                groups = new ArrayList<>();
            }
            return RespEntity.success(groups);
        } catch (Exception e) {
            logger.error("Error occurred while executing function <getGroups>: ", e);
            return RespEntity.error("Get groups failed");
        }
    }
}