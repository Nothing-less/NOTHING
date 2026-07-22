-- ============================================================
-- 文件仓库系统 - 建表脚本
-- 数据库：MySQL 8.0+
-- ============================================================

-- 1. 用户文件仓库表
CREATE TABLE IF NOT EXISTS user_file (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL                COMMENT '所属用户ID',
    file_name   VARCHAR(255) NOT NULL                COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL                COMMENT '磁盘存储的UUID文件名',
    file_path   VARCHAR(500) NOT NULL                COMMENT '磁盘绝对路径',
    file_size   BIGINT       NOT NULL                COMMENT '文件大小(字节)',
    mime_type   VARCHAR(100)                          COMMENT 'MIME类型',
    upload_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    INDEX idx_user (user_id),
    INDEX idx_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户文件仓库';

-- 2. 文件分享（发送给好友）表
CREATE TABLE IF NOT EXISTS file_share (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    sender_id    BIGINT      NOT NULL                COMMENT '发送者用户ID',
    receiver_id  BIGINT      NOT NULL                COMMENT '接收者用户ID',
    file_id      BIGINT      NOT NULL                COMMENT '关联user_file.id',
    send_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    is_revoked   TINYINT     NOT NULL DEFAULT 0      COMMENT '是否已撤回 0=否 1=是',
    PRIMARY KEY (id),
    INDEX idx_sender   (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_send_time (send_time),
    CONSTRAINT fk_fs_file FOREIGN KEY (file_id) REFERENCES user_file(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='文件分享记录';

-- ============================================================
-- 可选：扩展 user 表字段（如果你的 user 表还没有这些字段）
-- ============================================================
-- ALTER TABLE user ADD COLUMN nickname      VARCHAR(50)  DEFAULT '用户'    COMMENT '昵称';
-- ALTER TABLE user ADD COLUMN avatar_path  VARCHAR(500) DEFAULT NULL      COMMENT '头像路径';
-- ALTER TABLE user ADD COLUMN user_status TINYINT      DEFAULT 1         COMMENT '在线状态';

-- ============================================================
-- 测试数据（可选）
-- ============================================================
-- INSERT INTO user_file(user_id, file_name, stored_name, file_path, file_size, mime_type)
-- VALUES (1, '测试文档.pdf', 'abc123.pdf', '/home/user/MuSong/files/1/abc123.pdf', 102400, 'application/pdf');
