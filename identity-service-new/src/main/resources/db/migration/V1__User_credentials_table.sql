create table user_credentials
(
    user_id serial not null primary key,
    user_name varchar(64) not null ,
    user_email varchar(120) not null,
    user_password varchar not null
);
