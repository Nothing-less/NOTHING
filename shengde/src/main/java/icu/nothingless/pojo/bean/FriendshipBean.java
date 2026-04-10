package icu.nothingless.pojo.bean;


import icu.nothingless.pojo.adapter.IFSAdapter;
import icu.nothingless.pojo.ibean.IUserBean;

public class FriendshipBean implements java.io.Serializable, IFSAdapter {
    private Long fsId;
    private Long userId;
    private String friendId;
    private Integer fsStatus;
    private String remark;
    private String groupName;
    private String applyMsg;
    private String createTime;
    private String agreeTime;

    private Long unreadMsgCount; // 非持久化字段，表示与该好友的未读消息数
    private IUserBean friendInfo; // 好友的详细信息

    public void setUnreadMsgCount(Long unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }
    public void setFriendInfo(IUserBean friendInfo) {
        this.friendInfo = friendInfo;
    }
    public Long getUnreadMsgCount() {
        return unreadMsgCount;
    }
    public IUserBean getFriendInfo() {
        return friendInfo;
    }
    
    // Getters
    @Override
    public Long getFsId() {
        return fsId;
    }
    @Override
    public Long  getUserId() {
        return userId;
    }
    @Override
    public String getFriendId() {
        return friendId;
    }
    @Override
    public Integer getFsStatus() {
        return fsStatus;
    }
    @Override
    public String getRemark() {
        return remark;
    }
    @Override
    public String getGroupName() {
        return groupName;
    }
    @Override
    public String getApplyMsg() {
        return applyMsg;
    }
    @Override
    public String getCreateTime() {
        return createTime;
    }
    @Override
    public String getAgreeTime() {
        return agreeTime;
    }

    // Setters
    @Override
    public void setFsId(Long  fsId) {
        this.fsId = fsId;
    }
    @Override
    public void setUserId(Long  userId) {
        this.userId = userId;
    }
    @Override
    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
    @Override
    public void setFsStatus(Integer fsStatus) {
        this.fsStatus = fsStatus;
    }
    @Override
    public void setRemark(String remark) {
        this.remark = remark;
    }
    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    @Override
    public void setApplyMsg(String applyMsg) {
        this.applyMsg = applyMsg;
    }
    @Override
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    @Override
    public void setAgreeTime(String agreeTime) {
        this.agreeTime = agreeTime;
    }

}