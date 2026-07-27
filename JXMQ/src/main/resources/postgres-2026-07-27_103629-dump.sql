--
-- PostgreSQL database dump
--

\restrict FUtSdslanwRqdR71v2v5jD9wVgZrmrxRp080bGh6xdbvY6Fhh6L8kNanVgl9peL

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP TRIGGER IF EXISTS update_pages_updated_at ON public.pages;
DROP INDEX IF EXISTS public.idx_user;
DROP INDEX IF EXISTS public.idx_upload_time;
DROP INDEX IF EXISTS public.idx_sender;
DROP INDEX IF EXISTS public.idx_send_time;
DROP INDEX IF EXISTS public.idx_receiver;
DROP INDEX IF EXISTS public.idx_pages_status;
DROP INDEX IF EXISTS public.idx_pages_parent;
DROP INDEX IF EXISTS public.idx_pages_page_order;
DROP INDEX IF EXISTS public.idx_pages_page_name;
DROP INDEX IF EXISTS public.idx_pages_page_link;
DROP INDEX IF EXISTS public.idx_pages_page;
DROP INDEX IF EXISTS public.idx_fs_user;
DROP INDEX IF EXISTS public.idx_fs_friend;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.file_user DROP CONSTRAINT IF EXISTS user_file_pkey;
ALTER TABLE IF EXISTS ONLY public.t_friendship DROP CONSTRAINT IF EXISTS uk_user_friend_pending;
ALTER TABLE IF EXISTS ONLY public.t_message DROP CONSTRAINT IF EXISTS t_message_pkey;
ALTER TABLE IF EXISTS ONLY public.t_friendship DROP CONSTRAINT IF EXISTS t_friendship_user_id_friend_id_key;
ALTER TABLE IF EXISTS ONLY public.t_friendship DROP CONSTRAINT IF EXISTS t_friendship_pkey;
ALTER TABLE IF EXISTS ONLY public.pages DROP CONSTRAINT IF EXISTS pages_pkey;
ALTER TABLE IF EXISTS ONLY public.file_share DROP CONSTRAINT IF EXISTS file_share_pkey;
ALTER TABLE IF EXISTS public.t_message ALTER COLUMN msg_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.t_friendship ALTER COLUMN fs_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.file_user ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.file_share ALTER COLUMN id DROP DEFAULT;
DROP TABLE IF EXISTS public.users;
DROP SEQUENCE IF EXISTS public.users_id_seq;
DROP SEQUENCE IF EXISTS public.user_file_id_seq;
DROP SEQUENCE IF EXISTS public.t_message_msg_id_seq;
DROP TABLE IF EXISTS public.t_message;
DROP SEQUENCE IF EXISTS public.t_friendship_fs_id_seq;
DROP TABLE IF EXISTS public.t_friendship;
DROP TABLE IF EXISTS public.pages;
DROP TABLE IF EXISTS public.file_user;
DROP SEQUENCE IF EXISTS public.file_share_id_seq;
DROP TABLE IF EXISTS public.file_share;
DROP FUNCTION IF EXISTS public.update_updated_at_column();
DROP SCHEMA IF EXISTS public;
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at_column() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: file_share; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.file_share (
    id bigint NOT NULL,
    sender_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    file_id bigint NOT NULL,
    send_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_revoked smallint DEFAULT 0 NOT NULL,
    file_status boolean DEFAULT true NOT NULL
);


ALTER TABLE public.file_share OWNER TO postgres;

--
-- Name: TABLE file_share; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.file_share IS '文件分享记录';


--
-- Name: COLUMN file_share.sender_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_share.sender_id IS '发送者用户ID';


--
-- Name: COLUMN file_share.receiver_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_share.receiver_id IS '接收者用户ID';


--
-- Name: COLUMN file_share.file_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_share.file_id IS '关联file_user.id';


--
-- Name: COLUMN file_share.send_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_share.send_time IS '发送时间';


--
-- Name: COLUMN file_share.is_revoked; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_share.is_revoked IS '是否已撤回 0=否 1=是';


--
-- Name: COLUMN file_share.file_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_share.file_status IS 'TRUE为正常，FALSE为被删除';


--
-- Name: file_share_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.file_share_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.file_share_id_seq OWNER TO postgres;

--
-- Name: file_share_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.file_share_id_seq OWNED BY public.file_share.id;


--
-- Name: file_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.file_user (
    id bigint CONSTRAINT user_file_id_not_null NOT NULL,
    user_id bigint CONSTRAINT user_file_user_id_not_null NOT NULL,
    file_name character varying(255) CONSTRAINT user_file_file_name_not_null NOT NULL,
    stored_name character varying(255) CONSTRAINT user_file_stored_name_not_null NOT NULL,
    file_path character varying(500) CONSTRAINT user_file_file_path_not_null NOT NULL,
    file_size bigint CONSTRAINT user_file_file_size_not_null NOT NULL,
    mime_type character varying(100),
    upload_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP CONSTRAINT user_file_upload_time_not_null NOT NULL,
    file_status boolean DEFAULT true NOT NULL
);


ALTER TABLE public.file_user OWNER TO postgres;

--
-- Name: TABLE file_user; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.file_user IS '用户文件仓库';


--
-- Name: COLUMN file_user.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.user_id IS '所属用户ID';


--
-- Name: COLUMN file_user.file_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.file_name IS '原始文件名';


--
-- Name: COLUMN file_user.stored_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.stored_name IS '磁盘存储的UUID文件名';


--
-- Name: COLUMN file_user.file_path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.file_path IS '磁盘绝对路径';


--
-- Name: COLUMN file_user.file_size; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.file_size IS '文件大小(字节)';


--
-- Name: COLUMN file_user.mime_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.mime_type IS 'MIME类型';


--
-- Name: COLUMN file_user.upload_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.upload_time IS '上传时间';


--
-- Name: COLUMN file_user.file_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.file_user.file_status IS '当前记录行状态';


--
-- Name: pages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pages (
    page_id character varying(50) NOT NULL,
    page_link character varying(100) NOT NULL,
    page_name character varying(100) NOT NULL,
    page_order character varying(20),
    parent character varying(50),
    page_status boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.pages OWNER TO postgres;

--
-- Name: TABLE pages; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.pages IS '页面表';


--
-- Name: COLUMN pages.page_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.page_id IS '页面ID，主键，字符串类型';


--
-- Name: COLUMN pages.page_link; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.page_link IS '页面标识符/路径';


--
-- Name: COLUMN pages.page_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.page_name IS '页面名称';


--
-- Name: COLUMN pages.page_order; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.page_order IS '页面排序顺序，字符串类型';


--
-- Name: COLUMN pages.parent; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.parent IS '父页面ID';


--
-- Name: COLUMN pages.page_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.page_status IS '页面状态：TRUE-启用，FALSE-禁用';


--
-- Name: COLUMN pages.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.created_at IS '创建时间';


--
-- Name: COLUMN pages.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pages.updated_at IS '更新时间';


--
-- Name: t_friendship; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.t_friendship (
    fs_id bigint NOT NULL,
    user_id bigint NOT NULL,
    friend_id bigint NOT NULL,
    fs_status smallint DEFAULT 0,
    remark character varying(50),
    group_name character varying(50) DEFAULT '我的好友'::character varying,
    apply_msg character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    agree_time timestamp without time zone
);


ALTER TABLE public.t_friendship OWNER TO postgres;

--
-- Name: t_friendship_fs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.t_friendship_fs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.t_friendship_fs_id_seq OWNER TO postgres;

--
-- Name: t_friendship_fs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.t_friendship_fs_id_seq OWNED BY public.t_friendship.fs_id;


--
-- Name: t_message; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.t_message (
    msg_id bigint NOT NULL,
    sender_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    msg_type smallint DEFAULT 1,
    contents text,
    msg_status smallint DEFAULT 0,
    send_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    read_time timestamp without time zone
);


ALTER TABLE public.t_message OWNER TO postgres;

--
-- Name: t_message_msg_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.t_message_msg_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.t_message_msg_id_seq OWNER TO postgres;

--
-- Name: t_message_msg_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.t_message_msg_id_seq OWNED BY public.t_message.msg_id;


--
-- Name: user_file_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_file_id_seq OWNER TO postgres;

--
-- Name: user_file_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_file_id_seq OWNED BY public.file_user.id;


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id character varying(64) DEFAULT (nextval('public.users_id_seq'::regclass))::text NOT NULL,
    useraccount character varying(64),
    userpasswd character varying(255),
    nickname character varying(64),
    user_infos text,
    register_time character varying(20),
    last_login_time character varying(20),
    last_login_ip_addr character varying(45),
    user_status boolean DEFAULT true,
    role_id character varying(64),
    user_key1 character varying(255),
    user_key2 character varying(255),
    user_key3 character varying(255),
    user_key4 character varying(255),
    user_key5 character varying(255),
    user_key6 character varying(255)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: file_share id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.file_share ALTER COLUMN id SET DEFAULT nextval('public.file_share_id_seq'::regclass);


--
-- Name: file_user id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.file_user ALTER COLUMN id SET DEFAULT nextval('public.user_file_id_seq'::regclass);


--
-- Name: t_friendship fs_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_friendship ALTER COLUMN fs_id SET DEFAULT nextval('public.t_friendship_fs_id_seq'::regclass);


--
-- Name: t_message msg_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_message ALTER COLUMN msg_id SET DEFAULT nextval('public.t_message_msg_id_seq'::regclass);


--
-- Data for Name: file_share; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.file_share (id, sender_id, receiver_id, file_id, send_time, is_revoked, file_status) FROM stdin;
1	7	8	1	2026-07-23 03:17:18.319577	0	t
\.


--
-- Data for Name: file_user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.file_user (id, user_id, file_name, stored_name, file_path, file_size, mime_type, upload_time, file_status) FROM stdin;
1	1	测试文档.pdf	abc123.pdf	/home/user/MuSong/files/1/abc123.pdf	102400	application/pdf	2026-07-23 03:11:22.766277	t
\.


--
-- Data for Name: pages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pages (page_id, page_link, page_name, page_order, parent, page_status, created_at, updated_at) FROM stdin;
page_001	dashboard	主页	1	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.609733
page_006	friend_list	好友列表	2	main_page	t	2026-04-02 05:23:58.185353	2026-04-08 06:44:16.44506
page_003	orders	订单管理			f	2026-04-02 05:23:58.185353	2026-05-11 08:36:17.881776
page_004	products	商品管理			f	2026-04-02 05:23:58.185353	2026-05-11 08:36:17.883977
page_005	analytics	数据分析			f	2026-04-02 05:23:58.185353	2026-05-11 08:36:17.885079
page_007	settings	系统设置			f	2026-04-02 05:23:58.185353	2026-05-11 08:36:17.885915
page_002	profile	个人资料	9	main_page	t	2026-04-02 05:23:58.185353	2026-07-17 02:43:47.793499
user_create	user_create	添加新用户	99	main_page	t	2026-07-14 03:32:02.6735	2026-07-21 02:15:09.553403
page_008	friend_search	添加好友	3	main_page	f	2026-05-07 07:48:04.393128	2026-07-21 02:31:50.826283
page_010	apply_list	好友申请	4	main_page	f	2026-05-12 06:40:19.146378	2026-07-21 03:31:05.860703
\.


--
-- Data for Name: t_friendship; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.t_friendship (fs_id, user_id, friend_id, fs_status, remark, group_name, apply_msg, create_time, agree_time) FROM stdin;
13	8	7	1	\N	我的好友	我是Shengde_Yi	2026-07-09 15:36:02.70647	2026-07-09 15:36:42.234245
14	7	8	1	圣德	我的好友	\N	2026-07-09 15:36:42.234245	2026-07-09 15:36:42.234245
16	9	8	1	\N	我的好友	我是XQ	2026-07-09 15:38:20.596471	2026-07-09 15:38:32.280434
17	8	9	1	xxqq	我的好友	\N	2026-07-09 15:38:32.280434	2026-07-09 15:38:32.280434
22	9	7	1	\N	我的好友	我是 XQ	2026-07-22 09:30:55.998044	2026-07-22 09:32:27.212122
23	7	9	1		我的好友	\N	2026-07-22 09:32:27.212122	2026-07-22 09:32:27.212122
\.


--
-- Data for Name: t_message; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.t_message (msg_id, sender_id, receiver_id, msg_type, contents, msg_status, send_time, read_time) FROM stdin;
1	8	7	1	您好	1	2026-07-09 09:46:22.269528	2026-07-09 09:54:01.482581
2	8	7	1	您好	1	2026-07-09 09:53:20.794551	2026-07-09 09:54:01.482581
3	7	8	1	我真的好吗	1	2026-07-09 09:54:14.346159	2026-07-09 09:54:14.381898
4	7	8	1	你好吗	1	2026-07-09 09:54:34.225497	2026-07-09 09:54:34.332397
13	7	8	1	你还在吗	1	2026-07-09 16:14:04.857367	2026-07-09 16:16:57.006298
14	7	8	1	嘻嘻	1	2026-07-10 10:45:30.767825	2026-07-10 10:49:22.983112
15	8	9	1	1	1	2026-07-10 10:49:37.82876	2026-07-10 10:52:11.33628
16	7	8	1	下午好！	1	2026-07-20 16:28:09.13292	2026-07-21 09:21:28.775817
17	7	8	1	我好想你	1	2026-07-21 09:21:07.66556	2026-07-21 09:21:28.775817
18	8	7	1	我真的好想你	1	2026-07-21 09:21:45.485658	2026-07-21 09:38:38.45011
19	9	7	1	您好	1	2026-07-22 09:26:46.063157	2026-07-22 09:27:01.37192
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, useraccount, userpasswd, nickname, user_infos, register_time, last_login_time, last_login_ip_addr, user_status, role_id, user_key1, user_key2, user_key3, user_key4, user_key5, user_key6) FROM stdin;
15	testman01	384fde3636e6e01e0194d2976d8f26410af3e846e573379cb1a09e2f0752d8cc	\N	\N	1970-01-01 00:00:00	2026-07-16 13:11:50	0:0:0:0:0:0:0:1	t	player	OFFLINE	\N	\N	\N	\N	\N
9	xq	384fde3636e6e01e0194d2976d8f26410af3e846e573379cb1a09e2f0752d8cc	XQ	\N	\N	2026-07-22 09:30:46	0:0:0:0:0:0:0:1	t	player	OFFLINE	/MuSong/user/avatar/87d0d5e2869749cfa3968d61254c03ce.jpg	\N	\N	\N	\N
7	sakurai	633cd6e1e0af30e30871fb3bcaee0750832a3d51bb33112850bb899dc9fba614	Sakurai_Shengde	\N	\N	2026-07-22 09:31:30	0:0:0:0:0:0:0:1	t	SSV Administrator	OFFLINE	/MuSong/user/avatar/6a7596a31b6c4e18b39a9e5442084385.PNG	\N	\N	\N	\N
8	shengde	633cd6e1e0af30e30871fb3bcaee0750832a3d51bb33112850bb899dc9fba614	Shengde_Yi	\N	\N	2026-07-21 10:29:17	0:0:0:0:0:0:0:1	t	Super Administrator	OFFLINE	\N	\N	\N	\N	\N
\.


--
-- Name: file_share_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.file_share_id_seq', 1, true);


--
-- Name: t_friendship_fs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.t_friendship_fs_id_seq', 23, true);


--
-- Name: t_message_msg_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.t_message_msg_id_seq', 19, true);


--
-- Name: user_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_file_id_seq', 1, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 15, true);


--
-- Name: file_share file_share_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.file_share
    ADD CONSTRAINT file_share_pkey PRIMARY KEY (id);


--
-- Name: pages pages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pages
    ADD CONSTRAINT pages_pkey PRIMARY KEY (page_id);


--
-- Name: t_friendship t_friendship_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_friendship
    ADD CONSTRAINT t_friendship_pkey PRIMARY KEY (fs_id);


--
-- Name: t_friendship t_friendship_user_id_friend_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_friendship
    ADD CONSTRAINT t_friendship_user_id_friend_id_key UNIQUE (user_id, friend_id);


--
-- Name: t_message t_message_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_message
    ADD CONSTRAINT t_message_pkey PRIMARY KEY (msg_id);


--
-- Name: t_friendship uk_user_friend_pending; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_friendship
    ADD CONSTRAINT uk_user_friend_pending UNIQUE (user_id, friend_id, fs_status);


--
-- Name: file_user user_file_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.file_user
    ADD CONSTRAINT user_file_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: idx_fs_friend; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fs_friend ON public.t_friendship USING btree (friend_id, fs_status);


--
-- Name: idx_fs_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_fs_user ON public.t_friendship USING btree (user_id, fs_status);


--
-- Name: idx_pages_page; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pages_page ON public.pages USING btree (page_link);


--
-- Name: idx_pages_page_link; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pages_page_link ON public.pages USING btree (page_link);


--
-- Name: idx_pages_page_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pages_page_name ON public.pages USING btree (page_name);


--
-- Name: idx_pages_page_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pages_page_order ON public.pages USING btree (page_order);


--
-- Name: idx_pages_parent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pages_parent ON public.pages USING btree (parent);


--
-- Name: idx_pages_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pages_status ON public.pages USING btree (page_status);


--
-- Name: idx_receiver; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_receiver ON public.t_message USING btree (receiver_id, msg_status);


--
-- Name: idx_send_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_send_time ON public.file_share USING btree (send_time);


--
-- Name: idx_sender; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sender ON public.t_message USING btree (sender_id, send_time);


--
-- Name: idx_upload_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_upload_time ON public.file_user USING btree (upload_time);


--
-- Name: idx_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user ON public.file_user USING btree (user_id);


--
-- Name: pages update_pages_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_pages_updated_at BEFORE UPDATE ON public.pages FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- PostgreSQL database dump complete
--

\unrestrict FUtSdslanwRqdR71v2v5jD9wVgZrmrxRp080bGh6xdbvY6Fhh6L8kNanVgl9peL

