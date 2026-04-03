package icu.nothingless.pojo.bean;


import icu.nothingless.pojo.adapter.iFSAdapter;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;

public class Friendship implements java.io.Serializable, iFSAdapter{
    private String fsId;
    private String userId;
    private String friendId;
    private Integer fsStatus;
    private String remark;
    private String groupName;
    private String applyMsg;
    private String createTime;
    private String agreeTime;
    private Long unreadMsgCount; // 非持久化字段，表示与该好友的未读消息数

    public static final int STATUS_PENDING = 0;   // 待确认
    public static final int STATUS_AGREED = 1;    // 已同意
    public static final int STATUS_REJECTED = 2;  // 已拒绝
    public static final int STATUS_DELETED = 3;   // 已删除
    private iUserSTOAdapter friendInfo; // 好友的详细信息
    
    // Getters
    public String getFsId() {
        return fsId;
    }
    public String getUserId() {
        return userId;
    }
    public String getFriendId() {
        return friendId;
    }
    public Integer getFsStatus() {
        return fsStatus;
    }
    public String getRemark() {
        return remark;
    }
    public String getGroupName() {
        return groupName;
    }
    public String getApplyMsg() {
        return applyMsg;
    }
    public String getCreateTime() {
        return createTime;
    }
    public String getAgreeTime() {
        return agreeTime;
    }
    public static int getStatusPending() {
        return STATUS_PENDING;
    }
    public static int getStatusAgreed() {
        return STATUS_AGREED;
    }
    public static int getStatusRejected() {
        return STATUS_REJECTED;
    }
    public static int getStatusDeleted() {
        return STATUS_DELETED;
    }
    public iUserSTOAdapter getFriendInfo() {
        return friendInfo;
    }
    
    // Setters
    public void setFsId(String fsId) {
        this.fsId = fsId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
    public void setFsStatus(Integer fsStatus) {
        this.fsStatus = fsStatus;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public void setApplyMsg(String applyMsg) {
        this.applyMsg = applyMsg;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public void setAgreeTime(String agreeTime) {
        this.agreeTime = agreeTime;
    }
    public void setFriendInfo(iUserSTOAdapter friendInfo) {
        this.friendInfo = friendInfo;
    }
    public Long getUnreadMsgCount() {
        return unreadMsgCount;
    }
    public void setUnreadMsgCount(Long unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

    

}