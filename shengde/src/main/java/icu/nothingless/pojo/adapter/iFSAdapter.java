package icu.nothingless.pojo.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import icu.nothingless.pojo.bean.Friendship;
import icu.nothingless.pojo.engine.BaseEngine;
import icu.nothingless.pojo.engine.FSEngine;

@JsonDeserialize(as = Friendship.class)
public interface iFSAdapter extends iSTAdapter<iFSAdapter>{
    @Override
    default Long save() throws Exception{
        return BaseEngine.getInstance(FSEngine.class).save(this);
    }

    @Override
    default Long delete() throws Exception{
        return BaseEngine.getInstance(FSEngine.class).delete(this);
    }

    @Override
    default List<iFSAdapter> query() throws Exception{
        return BaseEngine.getInstance(FSEngine.class).query(this);
    }

    /********************** 业务相关接口 **********************/
    default List<String> getGroups(Long userId) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).getGroups(userId);
    }
    default boolean updateFriendInfo(Long userId, Long friendId, String remark, String groupName) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).updateFriendInfo(userId, friendId, remark, groupName);
    }
    default boolean deleteFriend(Long userId, Long friendId) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).deleteFriend(userId, friendId);
    }
    default boolean rejectFriend(Long userId, Long friendId) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).rejectFriend(userId, friendId);
    }
    default boolean agreeFriend(Long userId, Long friendId, String remark, String groupName) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).agreeFriend(userId, friendId, groupName, groupName);
    }
    default List<Friendship> getPendingRequests(Long userId) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).getPendingRequests(userId);
    }
    default List<Friendship> getFriendList(Long userId, String groupName, String keyword) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).getFriendList(userId, groupName, keyword);
    }
    default boolean isFriend(Long userId, Long friendId) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).isFriend(userId, friendId);
    }
    default List<Friendship> getFriendship(Long userId, Long friendId) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).getFriendship(userId, friendId);
    }
    default boolean applyFriend(Long userId, Long friendId, String applyMsg) throws Exception{
        return BaseEngine.getInstance(FSEngine.class).applyFriend(userId, friendId, applyMsg);
    }
}
