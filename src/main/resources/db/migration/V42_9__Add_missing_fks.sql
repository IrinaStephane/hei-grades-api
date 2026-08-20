alter table exam
    add constraint exam_course_assignment_fk
        foreign key (course_assignment_id) references course_assignments (id);

alter table grade
    add constraint grade_student_fk
        foreign key (student_id) references users (id);
