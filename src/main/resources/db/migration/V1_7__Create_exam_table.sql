create table if not exists exam
(
    id                   varchar not null
    constraint exam_pk primary key,
    course_assignment_id varchar not null,
    title                varchar not null,
    examination_date     timestamp not null,
    coefficient          decimal not null
    constraint exam_coefficient_check check (coefficient > 0 and coefficient <= 1)
    );

create index if not exists idx_exam_course_assignment on exam (course_assignment_id);