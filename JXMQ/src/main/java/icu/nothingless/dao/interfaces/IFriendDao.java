package icu.nothingless.dao.interfaces;

import java.util.List;

import icu.nothingless.commons.R;
import icu.nothingless.pojo.adapter.IFSAdapter;

public interface IFriendDao<T extends IFSAdapter> {

    // 申请添加好友
    R<Boolean> applyFriend(Long userId, Long friendId, String applyMsg)throws Exception;

    // 获取单向好友关系
    R<List<T>> getFriendship(Long userId, Long friendId)throws Exception;

    // 获取双向好友关系(检查是否已经是好友)
    R<Boolean> isFriend(Long userId, Long friendId)throws Exception;

    // 获取好友列表(包含好友信息)
    R<List<T>> getFriendList(Long userId, String groupName, String keyword)throws Exception;

    // 获取待处理的好友申请列表
    R<List<T>> getPendingRequests(Long userId)throws Exception;

    // 同意好友申请(双向建立关系)
    R<Boolean> agreeFriend(Long userId, Long friendId, String remark, String groupName)throws Exception;

    // 拒绝好友申请
    R<Boolean> rejectFriend(Long userId, Long friendId)throws Exception;

    // 删除好友(双向删除)
    R<Boolean> deleteFriend(Long userId, Long friendId)throws Exception;

    // 修改备注和分组
    R<Boolean> updateFriendInfo(Long userId, Long friendId, String remark, String groupName)throws Exception;

    // 获取分组列表
    R<List<String>> getGroups(Long userId)throws Exception;

}