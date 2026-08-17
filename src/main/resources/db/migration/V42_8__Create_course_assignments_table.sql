create table if not exists course_assignments
(
    id         varchar
        constraint course_assignments_pk primary key,
    course_id  varchar not null
        constraint course_assignments_course_id_fk references courses (id),
    teacher_id varchar not null
        constraint course_assignments_teacher_id_fk references users (id),
    group_id   varchar not null
        constraint course_assignments_group_id_fk references groups (id),
    year       integer not null,
    semester   integer not null
);
