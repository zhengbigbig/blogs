create table user
(
    id                 bigint primary key auto_increment,
    username           varchar(100) unique not null,
    encrypted_password varchar(100)        not null,
    avatar             varchar(100),
    email              varchar(100) unique not null,
    sex                tinyint default 0,
    summary            varchar(100),
    profession         varchar(100),
    address            varchar(100),
    technology_stack   varchar(100),
    created_at         timestamp,
    updated_at         timestamp,
    state              tinyint default 1
);

create table email_sms
(
    id         bigint primary key auto_increment,
    email      varchar(100) not null,
    sms        int          not null,
    usable     tinyint      not null default 1 comment '是否有效，1-有效，0-无效',
    created_at timestamp,
    dead_line  timestamp
);

#mysql 定时任务
DROP event IF EXISTS e_delete_sms;
CREATE EVENT e_delete_sms
    ON SCHEDULE
        EVERY 3 MINUTE
    DO
    DELETE
    FROM email_sms
    WHERE dead_line < current_timestamp;

# 角色
create table sys_role
(
    id   bigint primary key auto_increment,
    name varchar(100) not null
);
create table sys_role_user
(
    id      bigint primary key auto_increment,
    user_id bigint not null,
    role_id bigint not null
);

#权限
create table sys_permission_role
(
    id            bigint primary key auto_increment,
    role_id       bigint not null,
    permission_id bigint not null
);
create table sys_permission
(
    id          bigint primary key auto_increment,
    name        varchar(100) not null,
    description varchar(100) not null,
    url         varchar(100) not null,
    pid         int
);

#初始化权限
insert into sys_role (name)
values ('ROLE_ADMIN');
insert into sys_role (name)
values ('ROLE_EXECUTOR');
insert into sys_role (name)
values ('ROLE_USER');
insert into sys_role_user (user_id, role_id)
values (1, 1);

insert into sys_permission (id, name, description, url, pid)
values (1, 'ROLE_MANAGER_SESSION', '管理session', '/session/**', null);
insert into sys_permission_role (role_id, permission_id)
values (1, 1);
