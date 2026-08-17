create table if not exists group_flows
(
    id            varchar
        constraint group_flows_pk primary key,
    group_id      varchar not null
        constraint group_flows_group_id_fk references groups (id),
    student_id    varchar not null
        constraint group_flows_student_id_fk references users (id),
    flow_type     varchar not null,
    flow_datetime timestamp not null
);
