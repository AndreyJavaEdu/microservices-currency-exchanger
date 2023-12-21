create table ACCOUNT_EVENT
(
    uuid varchar(120) not null,
    user_id bigint not null,
    account_id bigint not null,
    from_account bigint,
    currency_code varchar(3) not null,
    operation_code smallint not null,
    amount bigint not null,
    date_creation_event timestamp not null,
    primary key (uuid, account_id)
);
