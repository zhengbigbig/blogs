create table Blog
(
    id          bigint primary key auto_increment,
    user_id     bigint,
    title       varchar(100),
    description varchar(100),
    content     TEXT,
    updated_at  timestamp,
    created_at  timestamp
)