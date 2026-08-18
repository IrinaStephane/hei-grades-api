create table if not exists users
(
    id            varchar
        constraint users_pk primary key,
    first_name    varchar not null,
    last_name     varchar not null,
    email         varchar not null
        constraint users_email_unique unique,
    password_hash varchar not null,
    role          varchar not null,
    created_at    timestamp not null
);
