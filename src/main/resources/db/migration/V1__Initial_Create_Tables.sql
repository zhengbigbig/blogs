create table user
(
    id                 bigint primary key auto_increment,
    username           varchar(100),
    encrypted_password varchar(100),
    avatar             varchar(100),
    created_at         timestamp,
    updated_at         timestamp
)