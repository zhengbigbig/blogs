create table user
(
    id                 bigint primary key auto_increment,
    username           varchar(100) unique not null ,
    encrypted_password varchar(100) not null ,
    avatar             varchar(100),
    email               varchar(100) unique not null ,
    sex                 tinyint default 0,
    summary             varchar(100),
    profession          varchar(100),
    address             varchar(100),
    technology_stack    varchar(100),
    created_at         timestamp,
    updated_at         timestamp,
    state               tinyint default 1
);

create table sms
(
    id         bigint primary key auto_increment,
    email       varchar(100) not null ,
    sms        int not null ,
    usable tinyint not null comment '是否有效，1-无效，2-有效',
    created_at timestamp,
    dead_line  timestamp
);

#mysql 定时任务
DROP event IF EXISTS e_delete_sms;
CREATE EVENT e_delete_sms
    ON SCHEDULE
        EVERY 3 MINUTE
    DO
    DELETE FROM sms WHERE dead_line < current_timestamp;