create table if not exists promotions
(
    id         varchar
        constraint promotions_pk primary key,
    ref        varchar not null
        constraint promotions_ref_unique unique,
    entry_year integer not null
);
