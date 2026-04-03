package icu.nothingless.dao.impl.LoginDaoImpl;

import icu.nothingless.dao.interfaces.iFriendDao;
import icu.nothingless.pojo.bean.Friendship;
import icu.nothingless.pojo.engine.FSEngine;
import java.util.List;

public class FriendDaoImpl implements iFriendDao {
    private static final FSEngine engine = new FSEngine();
    
    // 申请添加好友
    @Override
    public boolean applyFriend(Long userId, Long friendId, String applyMsg)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        if(userId.equals(friendId)) throw new IllegalArgumentException("Cannot add yourself as a friend");
        return engine.applyFriend(userId, friendId, applyMsg);
    }
    
    // 获取单向好友关系
    @Override
    public List<Friendship> getFriendship(Long userId, Long friendId)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        return engine.getFriendship(userId, friendId);
    }
    
    // 获取双向好友关系(检查是否已经是好友)
    @Override
    public boolean isFriend(Long userId, Long friendId)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        return engine.isFriend(userId, friendId);
    }
    
    // 获取好友列表(包含好友信息)
    @Override
    public List<Friendship> getFriendList(Long userId, String groupName, String keyword)throws Exception {
        if(userId == null) throw new IllegalArgumentException("User ID cannot be null");
        return engine.getFriendList(userId, groupName, keyword);
    }
    
    // 获取待处理的好友申请列表
    @Override
    public List<Friendship> getPendingRequests(Long userId)throws Exception {
        if(userId == null) throw new IllegalArgumentException("User ID cannot be null");
        return engine.getPendingRequests(userId);
    }
    
    // 同意好友申请(双向建立关系)
    @Override
    public boolean agreeFriend(Long userId, Long friendId, String remark, String groupName)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        return engine.agreeFriend(userId, friendId, remark, groupName);
    }
    
    // 拒绝好友申请
    @Override
    public boolean rejectFriend(Long userId, Long friendId)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        return engine.rejectFriend(userId, friendId);
    }
    
    // 删除好友(双向删除)
    @Override
    public boolean deleteFriend(Long userId, Long friendId)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        return engine.deleteFriend(userId, friendId);
    }
    
    // 修改备注和分组
    @Override
    public boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName)throws Exception {
        if(userId == null || friendId == null) throw new IllegalArgumentException("User ID and Friend ID cannot be null");
        return engine.updateFriendInfo(userId, friendId, remark, groupName);
    }
    
    // 获取分组列表
    @Override
    public List<String> getGroups(Long userId)throws Exception {
        if(userId == null) throw new IllegalArgumentException("User ID cannot be null");
        return engine.getGroups(userId);
    }
    
}