create table user
(
    id                 bigint primary key auto_increment,
    username           varchar(100) unique not null ,
    encrypted_password varchar(100) not null ,
    avatar             varchar(100),
    created_at         timestamp,
    updated_at         timestamp
)