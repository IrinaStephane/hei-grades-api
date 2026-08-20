alter table course_assignments
    drop constraint if exists course_assignments_course_id_fk,
    add constraint course_assignments_course_id_fk
        foreign key (course_id) references courses (id) on delete cascade;

alter table course_assignments
    drop constraint if exists course_assignments_teacher_id_fk,
    add constraint course_assignments_teacher_id_fk
        foreign key (teacher_id) references users (id) on delete cascade;

alter table course_assignments
    drop constraint if exists course_assignments_group_id_fk,
    add constraint course_assignments_group_id_fk
        foreign key (group_id) references groups (id) on delete cascade;

alter table exam
    drop constraint if exists exam_course_assignment_fk,
    add constraint exam_course_assignment_fk
        foreign key (course_assignment_id) references course_assignments (id) on delete cascade;

alter table grade
    drop constraint if exists grade_student_fk,
    add constraint grade_student_fk
        foreign key (student_id) references users (id) on delete cascade;

alter table grade
    drop constraint if exists grade_exam_fk,
    add constraint grade_exam_fk
        foreign key (exam_id) references exam (id) on delete cascade;

alter table grade_history
    drop constraint if exists grade_history_grade_fk,
    add constraint grade_history_grade_fk
        foreign key (grade_id) references grade (id) on delete cascade;
