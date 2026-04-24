package icu.nothingless.pojo.dto;

import icu.nothingless.pojo.bean.FriendshipBean;
import icu.nothingless.pojo.ibean.IFSBean;

/**
 * Friendship DTO类
 */
public class Friendship implements java.io.Serializable, IFSBean {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_AGREED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_DELETED = 3;

    private static final long serialVersionUID = 1L;

    private Long fsId;
    private Long userId;
    private String friendId;
    private Integer fsStatus;
    private String remark;
    private String groupName;
    private String applyMsg;
    private String createTime;
    private String agreeTime;
    private Long unreadMsgCount;
    private User friendInfo;

    public Friendship() {
        this.unreadMsgCount = 0L;
    }

    public Friendship(
            Long fsId,
            Long userId,
            String friendId,
            Integer fsStatus,
            String remark,
            String groupName,
            String applyMsg,
            String createTime,
            String agreeTime,
            Long unreadMsgCount,
            User friendInfo) {
        this.fsId = fsId;
        this.userId = userId;
        this.friendId = friendId;
        this.fsStatus = fsStatus;
        this.remark = remark;
        this.groupName = groupName;
        this.applyMsg = applyMsg;
        this.createTime = createTime;
        this.agreeTime = agreeTime;
        this.unreadMsgCount = unreadMsgCount;
        this.friendInfo = friendInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getFsId() {
        return fsId;
    }

    public void setFsId(Long fsId) {
        this.fsId = fsId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public Integer getFsStatus() {
        return fsStatus;
    }

    public void setFsStatus(Integer fsStatus) {
        this.fsStatus = fsStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getApplyMsg() {
        return applyMsg;
    }

    public void setApplyMsg(String applyMsg) {
        this.applyMsg = applyMsg;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getAgreeTime() {
        return agreeTime;
    }

    public void setAgreeTime(String agreeTime) {
        this.agreeTime = agreeTime;
    }

    public Long getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public void setUnreadMsgCount(Long unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

    public User getFriendInfo() {
        return friendInfo;
    }

    public void setFriendInfo(User friendInfo) {
        this.friendInfo = friendInfo;
    }

    // 可变 Builder
    public static class Builder {
        private Long fsId;
        private Long userId;
        private String friendId;
        private Integer fsStatus;
        private String remark;
        private String groupName;
        private String applyMsg;
        private String createTime;
        private String agreeTime;
        private Long unreadMsgCount = 0L; // 默认值
        private User friendInfo;

        public Builder fsId(Long fsId) {
            this.fsId = fsId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder friendId(String friendId) {
            this.friendId = friendId;
            return this;
        }

        public Builder fsStatus(Integer fsStatus) {
            this.fsStatus = fsStatus;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder applyMsg(String applyMsg) {
            this.applyMsg = applyMsg;
            return this;
        }

        public Builder createTime(String createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder agreeTime(String agreeTime) {
            this.agreeTime = agreeTime;
            return this;
        }

        public Builder unreadMsgCount(Long unreadMsgCount) {
            this.unreadMsgCount = unreadMsgCount;
            return this;
        }

        public Builder friendInfo(User friendInfo) {
            this.friendInfo = friendInfo;
            return this;
        }

        // 从 Entity 填充基础字段
        public Builder from(FriendshipBean entity) {
            this.fsId = entity.getFsId();
            this.userId = entity.getUserId();
            this.friendId = entity.getFriendId();
            this.fsStatus = entity.getFsStatus();
            this.remark = entity.getRemark();
            this.groupName = entity.getGroupName();
            this.applyMsg = entity.getApplyMsg();
            this.createTime = entity.getCreateTime();
            this.agreeTime = entity.getAgreeTime();
            return this;
        }

        public Friendship build() {
            // 必填校验
            if (fsId == null)
                throw new IllegalStateException("fsId required");
            if (userId == null)
                throw new IllegalStateException("userId required");

            return new Friendship(
                    fsId, userId, friendId, fsStatus, remark, groupName,
                    applyMsg, createTime, agreeTime, unreadMsgCount, friendInfo);
        }
    }
}
