CREATE TABLE users (
    user_id bigint NOT NULL,
    useraccount varchar(64),
    userpasswd varchar(255),
    nickname varchar(64),
    user_infos TEXT,
    register_time varchar(20),
    last_login_time varchar(20),
    last_login_ip_addr varchar(45),
    user_status boolean DEFAULT TRUE,
    role_id varchar(64),
    user_key1 varchar(255),
    user_key2 varchar(255),
    user_key3 varchar(255),
    user_key4 varchar(255),
    user_key5 varchar(255),
    user_key6 varchar(255),
    PRIMARY KEY (user_id)
);

CREATE TABLE t_friendship (
    fs_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    fs_status SMALLINT DEFAULT 0,
    remark VARCHAR(50),
    group_name VARCHAR(50) DEFAULT '我的好友',
    apply_msg VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    agree_time TIMESTAMP,
    UNIQUE(user_id, friend_id)  -- 用于 ON CONFLICT
);

-- 索引
CREATE INDEX idx_fs_user ON t_friendship(user_id, fs_status);
CREATE INDEX idx_fs_friend ON t_friendship(friend_id, fs_status);

-- 消息表
CREATE TABLE t_message (
    msg_id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    msg_type SMALLINT DEFAULT 1,
    contents TEXT,
    msg_status SMALLINT DEFAULT 0,
    send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_time TIMESTAMP
);
-- 索引
CREATE INDEX idx_sender ON t_message(sender_id, send_time);
CREATE INDEX idx_receiver ON t_message(receiver_id, msg_status);

-- 会话表(最近联系人)
CREATE TABLE t_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    target_type TINYINT DEFAULT 1 COMMENT '1用户 2群',
    last_msg_id BIGINT,
    last_msg_time DATETIME,
    unread_count INT DEFAULT 0,
    is_top TINYINT DEFAULT 0,
    is_mute TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_target (user_id, target_id),
    INDEX idx_user_time (user_id, last_msg_time)
);

--pages 
CREATE TABLE pages (
    page_id VARCHAR(50) PRIMARY KEY,
    page_link VARCHAR(100) NOT NULL,
    page_name VARCHAR(100) NOT NULL,
    page_order VARCHAR(20),
    parent VARCHAR(50),
    page_status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pages_parent ON pages(parent);
CREATE INDEX idx_pages_page_link ON pages(page_link);
CREATE INDEX idx_pages_page_name ON pages(page_name);

COMMENT ON TABLE pages IS '页面表';
COMMENT ON COLUMN pages.page_id IS '页面ID，主键，字符串类型';
COMMENT ON COLUMN pages.page_link IS '页面标识符/路径';
COMMENT ON COLUMN pages.page_name IS '页面名称';
COMMENT ON COLUMN pages.page_order IS '页面排序顺序，字符串类型';
COMMENT ON COLUMN pages.parent IS '父页面ID';
COMMENT ON COLUMN pages.page_status IS '页面状态：TRUE-启用，FALSE-禁用';
COMMENT ON COLUMN pages.created_at IS '创建时间';
COMMENT ON COLUMN pages.updated_at IS '更新时间';

CREATE INDEX idx_pages_status ON pages(page_status);
CREATE INDEX idx_pages_page_order ON pages(page_order);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_pages_updated_at
    BEFORE UPDATE ON pages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


pg_dump  -d postgres > backup.sql


# 先连接到 postgres 数据库创建新数据库
psql -h localhost -U username -d postgres -c "CREATE DATABASE new_dbname;"


psql -h localhost -U username -d new_dbname < backup.sql