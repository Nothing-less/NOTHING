--
-- PostgreSQL database dump
--

\restrict r615rv6NKEzTMxY5wMNEt2DbJHmtPhNZV9iErjXtKahzAlLmGgiBgsf4zjeugv9

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
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
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
-- Name: t_friendship fs_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_friendship ALTER COLUMN fs_id SET DEFAULT nextval('public.t_friendship_fs_id_seq'::regclass);


--
-- Name: t_message msg_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_message ALTER COLUMN msg_id SET DEFAULT nextval('public.t_message_msg_id_seq'::regclass);


--
-- Data for Name: pages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pages (page_id, page_link, page_name, page_order, parent, page_status, created_at, updated_at) FROM stdin;
page_001	dashboard	主页	1	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.609733
page_002	users	用户管理	2	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.628554
page_003	orders	订单管理	3	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.629918
page_004	products	商品管理	4	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.631161
page_005	analytics	数据分析	5	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.631914
page_006	example_tables	数据表门	6	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.632485
page_007	settings	系统设置	7	main_page	t	2026-04-02 05:23:58.185353	2026-04-02 05:58:14.632971
\.


--
-- Data for Name: t_friendship; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.t_friendship (fs_id, user_id, friend_id, fs_status, remark, group_name, apply_msg, create_time, agree_time) FROM stdin;
\.


--
-- Data for Name: t_message; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.t_message (msg_id, sender_id, receiver_id, msg_type, contents, msg_status, send_time, read_time) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, useraccount, userpasswd, nickname, user_infos, register_time, last_login_time, last_login_ip_addr, user_status, role_id, user_key1, user_key2, user_key3, user_key4, user_key5, user_key6) FROM stdin;
\.


--
-- Name: t_friendship_fs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.t_friendship_fs_id_seq', 1, false);


--
-- Name: t_message_msg_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.t_message_msg_id_seq', 1, false);


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
-- Name: idx_sender; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sender ON public.t_message USING btree (sender_id, send_time);


--
-- Name: pages update_pages_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_pages_updated_at BEFORE UPDATE ON public.pages FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- PostgreSQL database dump complete
--

\unrestrict r615rv6NKEzTMxY5wMNEt2DbJHmtPhNZV9iErjXtKahzAlLmGgiBgsf4zjeugv9

