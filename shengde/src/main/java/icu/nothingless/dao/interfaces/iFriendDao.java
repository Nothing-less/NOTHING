package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.pojo.adapter.IFSAdapter;

public interface IFriendDao<T extends IFSAdapter> {

    // 申请添加好友
    Boolean applyFriend(Long userId, Long friendId, String applyMsg)throws Exception;

    // 获取单向好友关系
    List<T> getFriendship(Long userId, Long friendId)throws Exception;

    // 获取双向好友关系(检查是否已经是好友)
    Boolean isFriend(Long userId, Long friendId)throws Exception;

    // 获取好友列表(包含好友信息)
    List<T> getFriendList(Long userId, String groupName, String keyword)throws Exception;

    // 获取待处理的好友申请列表
    List<T> getPendingRequests(Long userId)throws Exception;

    // 同意好友申请(双向建立关系)
    Boolean agreeFriend(Long userId, Long friendId, String remark, String groupName)throws Exception;

    // 拒绝好友申请
    Boolean rejectFriend(Long userId, Long friendId)throws Exception;

    // 删除好友(双向删除)
    Boolean deleteFriend(Long userId, Long friendId)throws Exception;

    // 修改备注和分组
    Boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName)throws Exception;

    // 获取分组列表
    List<String> getGroups(Long userId)throws Exception;

}