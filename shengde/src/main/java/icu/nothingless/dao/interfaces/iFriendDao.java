package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.pojo.bean.Friendship;

public interface iFriendDao {

    // 申请添加好友
    boolean applyFriend(Long userId, Long friendId, String applyMsg)throws Exception;

    // 获取单向好友关系
    List<Friendship> getFriendship(Long userId, Long friendId)throws Exception;

    // 获取双向好友关系(检查是否已经是好友)
    boolean isFriend(Long userId, Long friendId)throws Exception;

    // 获取好友列表(包含好友信息)
    List<Friendship> getFriendList(Long userId, String groupName, String keyword)throws Exception;

    // 获取待处理的好友申请列表
    List<Friendship> getPendingRequests(Long userId)throws Exception;

    // 同意好友申请(双向建立关系)
    boolean agreeFriend(Long userId, Long friendId, String remark, String groupName)throws Exception;

    // 拒绝好友申请
    boolean rejectFriend(Long userId, Long friendId)throws Exception;

    // 删除好友(双向删除)
    boolean deleteFriend(Long userId, Long friendId)throws Exception;

    // 修改备注和分组
    boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName)throws Exception;

    // 获取分组列表
    List<String> getGroups(Long userId)throws Exception;

}