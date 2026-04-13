package icu.nothingless.pojo.ibean;

import icu.nothingless.pojo.adapter.IUserAdapter;

public interface IFSBean {
    // Getters
    Long getFsId();

    Long getUserId();

    String getFriendId();

    Integer getFsStatus();

    String getRemark();

    String getGroupName();

    String getApplyMsg();

    String getCreateTime();

    String getAgreeTime();

    // Setters
    void setFsId(Long fsId);

    void setUserId(Long userId);

    void setFriendId(String friendId);

    void setFsStatus(Integer fsStatus);

    void setRemark(String remark);

    void setGroupName(String groupName);

    void setApplyMsg(String applyMsg);

    void setCreateTime(String createTime);

    void setAgreeTime(String agreeTime);

}