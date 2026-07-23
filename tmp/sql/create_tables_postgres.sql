-- ============================================================
-- 文件仓库系统 - 建表脚本
-- 数据库：PostgreSQL
-- ============================================================

-- 1. 用户文件仓库表
CREATE TABLE IF NOT EXISTS user_file (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    file_size   BIGINT       NOT NULL,
    mime_type   VARCHAR(100),
    upload_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_file IS '用户文件仓库';
COMMENT ON COLUMN user_file.user_id IS '所属用户ID';
COMMENT ON COLUMN user_file.file_name IS '原始文件名';
COMMENT ON COLUMN user_file.stored_name IS '磁盘存储的UUID文件名';
COMMENT ON COLUMN user_file.file_path IS '磁盘绝对路径';
COMMENT ON COLUMN user_file.file_size IS '文件大小(字节)';
COMMENT ON COLUMN user_file.mime_type IS 'MIME类型';
COMMENT ON COLUMN user_file.upload_time IS '上传时间';

CREATE INDEX IF NOT EXISTS idx_user ON user_file(user_id);
CREATE INDEX IF NOT EXISTS idx_upload_time ON user_file(upload_time);

-- 2. 文件分享（发送给好友）表
CREATE TABLE IF NOT EXISTS file_share (
    id           BIGSERIAL    PRIMARY KEY,
    sender_id    BIGINT       NOT NULL,
    receiver_id  BIGINT       NOT NULL,
    file_id      BIGINT       NOT NULL,
    send_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked   SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE file_share IS '文件分享记录';
COMMENT ON COLUMN file_share.sender_id IS '发送者用户ID';
COMMENT ON COLUMN file_share.receiver_id IS '接收者用户ID';
COMMENT ON COLUMN file_share.file_id IS '关联user_file.id';
COMMENT ON COLUMN file_share.send_time IS '发送时间';
COMMENT ON COLUMN file_share.is_revoked IS '是否已撤回 0=否 1=是';

CREATE INDEX IF NOT EXISTS idx_sender ON file_share(sender_id);
CREATE INDEX IF NOT EXISTS idx_receiver ON file_share(receiver_id);
CREATE INDEX IF NOT EXISTS idx_send_time ON file_share(send_time);

ALTER TABLE file_share
    ADD CONSTRAINT fk_fs_file FOREIGN KEY (file_id)
    REFERENCES user_file (id)
    ON DELETE CASCADE;

-- ============================================================
-- 可选：扩展 user 表字段（如果你的 user 表还没有这些字段）
-- ============================================================
-- ALTER TABLE "user" ADD COLUMN nickname      VARCHAR(50)  DEFAULT '用户';
-- ALTER TABLE "user" ADD COLUMN avatar_path  VARCHAR(500) DEFAULT NULL;
-- ALTER TABLE "user" ADD COLUMN user_status SMALLINT      DEFAULT 1;

-- ============================================================
-- 测试数据（可选）
-- ============================================================
INSERT INTO file_user(user_id, file_name, stored_name, file_path, file_size, mime_type)
 VALUES (1, '测试文档.pdf', 'abc123.pdf', '/home/user/MuSong/files/1/abc123.pdf', 102400, 'application/pdf');

insert into file_share(sender_id, receiver_id, file_id)
 values (1, 2, 1);

            SELECT fs.*, uf.file_name, uf.file_size,
                   su.nickname AS sender_name, ru.nickname AS receiver_name
            FROM file_share fs
            JOIN file_user uf ON fs.file_id = uf.id
            JOIN users su ON fs.sender_id = su.user_id::bigint
            JOIN users ru ON fs.receiver_id = ru.user_id::bigint
            WHERE fs.id = 1;


            SELECT fs.*, uf.file_name, uf.file_size,
                   su.nickname AS sender_name 
            FROM file_share fs 
            JOIN file_user uf ON fs.file_id = uf.id
            JOIN users su ON fs.sender_id = su.user_id::bigint
            WHERE fs.file_status AND uf.file_status AND fs.receiver_id = 8 AND fs.is_revoked = 0
            ORDER BY fs.send_time DESC