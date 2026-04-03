package icu.nothingless.service.interfaces;

import java.util.List;

import icu.nothingless.dto.UserDTO;
import icu.nothingless.pojo.bean.Friendship;
import icu.nothingless.pojo.bean.UserSTO;

public interface iFriendService {

    // 搜索用户
    List<UserDTO> searchUsers(Long userId, String keyword);

    // 申请添加好友
    boolean applyFriend(Long userId, Long friendId, String applyMsg);

    // 获取好友列表
    List<Friendship> getFriendList(Long userId, String group, String keyword);

    // 获取好友申请列表
    List<Friendship> getPendingRequests(Long userId);

    // 同意好友申请
    boolean agreeFriend(Long userId, Long friendId, String remark, String groupName);

    // 拒绝好友申请
    boolean rejectFriend(Long userId, Long friendId);

    // 删除好友
    boolean deleteFriend(Long userId, Long friendId);

    // 修改好友信息
    boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName);

    // 获取分组列表
    List<String> getGroups(Long userId);

}