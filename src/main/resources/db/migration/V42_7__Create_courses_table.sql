create table if not exists courses
(
    id      varchar
        constraint courses_pk primary key,
    code    varchar not null
        constraint courses_code_unique unique,
    title   varchar not null,
    credits integer not null
);
