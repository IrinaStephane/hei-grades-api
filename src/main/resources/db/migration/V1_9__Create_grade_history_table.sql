create table if not exists grade_history
(
    id         varchar not null
    constraint grade_history_pk primary key,
    grade_id   varchar not null
    constraint grade_history_grade_fk references grade (id),
    old_score  decimal,
    new_score  decimal not null,
    changed_at timestamp not null,
    comment    varchar
    );

create index if not exists idx_grade_history_grade on grade_history (grade_id);