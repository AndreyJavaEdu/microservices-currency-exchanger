create table OPERATION
(
    ID smallint not null primary key,
    OPERATION_CODE varchar(8) not null
);

INSERT INTO OPERATION(ID, OPERATION_CODE) VALUES (1, 'PUT'), (2, 'EXCHANGE');