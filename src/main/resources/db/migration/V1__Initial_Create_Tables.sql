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
    user_id    bigint not null,
    email       varchar(100) not null ,
    sms        int not null ,
    usable tinyint not null comment '是否有效，1-无效，2-有效',
    send tinyint not null comment '是否已发送，1-未发送，2-已发送',
    created_at timestamp,
    dead_line  timestamp
);