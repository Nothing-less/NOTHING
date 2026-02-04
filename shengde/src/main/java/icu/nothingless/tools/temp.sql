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