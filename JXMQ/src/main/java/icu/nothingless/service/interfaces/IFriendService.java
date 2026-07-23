package icu.nothingless.service.interfaces;

import java.util.List;

import icu.nothingless.commons.RespEntity;

public interface IFriendService<T,E> {

    // 搜索用户
    RespEntity<List<E>> searchUsers(Long userId, String keyword);

    // 申请添加好友
    RespEntity<Void> applyFriend(Long userId, Long friendId, String applyMsg);

    // 获取好友列表
    RespEntity<List<T>> getFriendList(Long userId, String group, String keyword);

    // 获取好友申请列表
    RespEntity<List<T>> getPendingRequests(Long userId);

    // 同意好友申请
    RespEntity<Void> agreeFriend(Long userId, Long friendId, String remark, String groupName);

    // 拒绝好友申请
    RespEntity<Void> rejectFriend(Long userId, Long friendId);

    // 删除好友
    RespEntity<Void> deleteFriend(Long userId, Long friendId);

    // 修改好友信息
    RespEntity<Void> updateFriendInfo(Long userId, Long friendId, String remark, String groupName);

    // 获取分组列表
    RespEntity<List<String>> getGroups(Long userId);

}