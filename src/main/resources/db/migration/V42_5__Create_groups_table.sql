create table if not exists groups
(
    id           varchar
        constraint groups_pk primary key,
    ref          varchar not null,
    path         varchar not null,
    promotion_id varchar not null
        constraint groups_promotion_id_fk references promotions (id)
);
