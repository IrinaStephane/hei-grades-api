create table if not exists grade
(
    id         varchar not null
    constraint grade_pk primary key,
    exam_id    varchar not null
    constraint grade_exam_fk references exam (id),
    student_id varchar not null,
    score      decimal not null
    constraint grade_score_check check (score >= 0 and score <= 20),
    is_final   boolean not null default false,
    constraint grade_unique_student_exam unique (exam_id, student_id)
    );

create index if not exists idx_grade_student on grade (student_id);
create index if not exists idx_grade_exam on grade (exam_id);