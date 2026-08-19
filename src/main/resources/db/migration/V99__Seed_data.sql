-- Demo seed data
-- Passwords: all users share the BCrypt hash of "password123"
do
$$
declare
  password_hash constant text := '$2y$10$vdZj4foBzTNEo18/LjlCOuAMhVDcelp9eZ1.FxLnFGZxS4y1McJfy';
begin
  -- ============================================================ users
  insert into users (id, first_name, last_name, email, password_hash, role, created_at) values
    ('admin_hei', 'Admin', 'HEI', 'admin@hei.school', password_hash, 'ADMIN', '2022-08-15 09:00:00'),
    ('teacher_rakoto', 'Mamy', 'Rakotomalala', 'mamy.rakotomalala@hei.school', password_hash, 'TEACHER', '2022-08-20 09:00:00'),
    ('teacher_andrianjatovo', 'Fara', 'Andrianjatovo', 'fara.andrianjatovo@hei.school', password_hash, 'TEACHER', '2022-08-20 09:00:00'),
    -- promotion 2022 (3rd year in 2024-2025)
    ('s22_lova', 'Lova', 'Randrianarisoa', 'lova.randrianarisoa@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_tiana', 'Tiana', 'Rakotomalala', 'tiana.rakotomalala@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_miora', 'Miora', 'Andrianjaka', 'miora.andrianjaka@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_toky', 'Toky', 'Rasoanaivo', 'toky.rasoanaivo@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_naina', 'Naina', 'Andriamasinoro', 'naina.andriamasinoro@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_feno', 'Feno', 'Rabeantoandro', 'feno.rabeantoandro@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_hery', 'Hery', 'Ramanantsoa', 'hery.ramanantsoa@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('s22_sitraka', 'Sitraka', 'Randriamampionona', 'sitraka.randriamampionona@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    -- promotion 2023 (2nd year in 2024-2025)
    ('s23_andry', 'Andry', 'Ravoahangy', 'andry.ravoahangy@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_hasina', 'Hasina', 'Rakotoniaina', 'hasina.rakotoniaina@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_fanja', 'Fanja', 'Ramananandrasana', 'fanja.ramananandrasana@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_rina', 'Rina', 'Andrianarivelo', 'rina.andrianarivelo@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_tahiry', 'Tahiry', 'Razafindrakoto', 'tahiry.razafindrakoto@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_vonjy', 'Vonjy', 'Randriambololona', 'vonjy.randriambololona@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_zo', 'Zo', 'Rasolofoniaina', 'zo.rasolofoniaina@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('s23_lala', 'Lala', 'Rakotondrazaka', 'lala.rakotondrazaka@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00');

  -- ============================================================ promotions
  insert into promotions (id, ref, entry_year) values
    ('promo2022', '2022', 2022),
    ('promo2023', '2023', 2023);

  -- ============================================================ groups
  insert into groups (id, ref, path, promotion_id) values
    ('group22_k1', 'K1', 'EL', 'promo2022'),
    ('group22_k2', 'K2', 'EL', 'promo2022'),
    ('group22_tn1', 'TN1', 'TN', 'promo2022'),
    ('group23_k1', 'K1', 'EL', 'promo2023'),
    ('group23_k2', 'K2', 'EL', 'promo2023'),
    ('group23_tn1', 'TN1', 'TN', 'promo2023');

  -- ============================================================ courses
  -- 6 courses per semester, 30 credits per semester (60 credits per year,
  -- 180 credits over the 3-year curriculum).
  create temp table seed_courses (code text primary key, title text, credits int) on commit drop;
  insert into seed_courses values
    -- L1
    ('PROG1', 'Programmation I', 8), ('MATH1', 'Mathématiques I', 6), ('SYS1', 'Systèmes I', 4),
    ('ANG1', 'Anglais I', 4), ('RES1', 'Réseaux I', 4), ('WEB1', 'Développement web I', 4),
    ('PROG2', 'Programmation II', 8), ('MATH2', 'Mathématiques II', 6), ('BDD1', 'Bases de données I', 6),
    ('COM1', 'Communication I', 4), ('GPAO1', 'Gestion de projets', 4), ('ANG2', 'Anglais II', 2),
    -- L2
    ('PROG3', 'Programmation III', 8), ('SYS2', 'Systèmes II', 6), ('BDD2', 'Bases de données II', 4),
    ('STAT1', 'Statistiques', 4), ('ARCH1', 'Architecture logicielle', 4), ('SEC1', 'Sécurité I', 4),
    ('PROG4', 'Programmation IV', 8), ('SYS3', 'Systèmes III', 6), ('WEB2', 'Développement web II', 6),
    ('RES2', 'Réseaux II', 4), ('AI1', 'Intelligence artificielle I', 4), ('COM2', 'Communication II', 2),
    -- L3 common
    ('AI2', 'Intelligence artificielle II', 6), ('SEC2', 'Sécurité II', 4), ('MEM1', 'Mémoire I', 4),
    ('RES3', 'Réseaux III', 2), ('AI3', 'Intelligence artificielle III', 6), ('SEC3', 'Sécurité III', 4),
    ('MEM2', 'Mémoire II', 4), ('COM3', 'Communication III', 2),
    -- L3 EL only
    ('PROG5', 'Programmation avancée', 8), ('LOG1', 'Logique', 6),
    ('PROG6', 'Programmation des systèmes', 8), ('LOG2', 'Logique II', 6),
    -- L3 TN only
    ('TRA1', 'Télécommunications I', 8), ('IOT1', 'Internet des objets I', 6),
    ('TRA2', 'Télécommunications II', 8), ('IOT2', 'Internet des objets II', 6);

  insert into courses (id, code, title, credits)
  select 'course_' || lower(code) || '_id', code, title, credits from seed_courses;

  -- ============================================================ course_assignments
  -- track = 'BOTH' (all groups), 'EL' (EL groups), 'TN' (TN group).
  -- promotion = the promotion whose groups take this course that year.
  create temp table seed_assignments (code text, year int, semester int, track text, promotion text) on commit drop;
  insert into seed_assignments values
    -- L1 for promo 2022
    ('PROG1', 2022, 1, 'BOTH', 'promo2022'), ('MATH1', 2022, 1, 'BOTH', 'promo2022'), ('SYS1', 2022, 1, 'BOTH', 'promo2022'),
    ('ANG1', 2022, 1, 'BOTH', 'promo2022'), ('RES1', 2022, 1, 'BOTH', 'promo2022'), ('WEB1', 2022, 1, 'BOTH', 'promo2022'),
    ('PROG2', 2022, 2, 'BOTH', 'promo2022'), ('MATH2', 2022, 2, 'BOTH', 'promo2022'), ('BDD1', 2022, 2, 'BOTH', 'promo2022'),
    ('COM1', 2022, 2, 'BOTH', 'promo2022'), ('GPAO1', 2022, 2, 'BOTH', 'promo2022'), ('ANG2', 2022, 2, 'BOTH', 'promo2022'),
    -- L2 for promo 2022
    ('PROG3', 2023, 1, 'BOTH', 'promo2022'), ('SYS2', 2023, 1, 'BOTH', 'promo2022'), ('BDD2', 2023, 1, 'BOTH', 'promo2022'),
    ('STAT1', 2023, 1, 'BOTH', 'promo2022'), ('ARCH1', 2023, 1, 'BOTH', 'promo2022'), ('SEC1', 2023, 1, 'BOTH', 'promo2022'),
    ('PROG4', 2023, 2, 'BOTH', 'promo2022'), ('SYS3', 2023, 2, 'BOTH', 'promo2022'), ('WEB2', 2023, 2, 'BOTH', 'promo2022'),
    ('RES2', 2023, 2, 'BOTH', 'promo2022'), ('AI1', 2023, 2, 'BOTH', 'promo2022'), ('COM2', 2023, 2, 'BOTH', 'promo2022'),
    -- L3 for promo 2022
    ('PROG5', 2024, 1, 'EL', 'promo2022'), ('LOG1', 2024, 1, 'EL', 'promo2022'), ('AI2', 2024, 1, 'BOTH', 'promo2022'),
    ('SEC2', 2024, 1, 'BOTH', 'promo2022'), ('MEM1', 2024, 1, 'BOTH', 'promo2022'), ('RES3', 2024, 1, 'BOTH', 'promo2022'),
    ('PROG6', 2024, 2, 'EL', 'promo2022'), ('LOG2', 2024, 2, 'EL', 'promo2022'), ('AI3', 2024, 2, 'BOTH', 'promo2022'),
    ('SEC3', 2024, 2, 'BOTH', 'promo2022'), ('MEM2', 2024, 2, 'BOTH', 'promo2022'), ('COM3', 2024, 2, 'BOTH', 'promo2022'),
    ('TRA1', 2024, 1, 'TN', 'promo2022'), ('IOT1', 2024, 1, 'TN', 'promo2022'),
    ('TRA2', 2024, 2, 'TN', 'promo2022'), ('IOT2', 2024, 2, 'TN', 'promo2022'),
    -- L1 for promo 2023
    ('PROG1', 2023, 1, 'BOTH', 'promo2023'), ('MATH1', 2023, 1, 'BOTH', 'promo2023'), ('SYS1', 2023, 1, 'BOTH', 'promo2023'),
    ('ANG1', 2023, 1, 'BOTH', 'promo2023'), ('RES1', 2023, 1, 'BOTH', 'promo2023'), ('WEB1', 2023, 1, 'BOTH', 'promo2023'),
    ('PROG2', 2023, 2, 'BOTH', 'promo2023'), ('MATH2', 2023, 2, 'BOTH', 'promo2023'), ('BDD1', 2023, 2, 'BOTH', 'promo2023'),
    ('COM1', 2023, 2, 'BOTH', 'promo2023'), ('GPAO1', 2023, 2, 'BOTH', 'promo2023'), ('ANG2', 2023, 2, 'BOTH', 'promo2023'),
    -- L2 for promo 2023
    ('PROG3', 2024, 1, 'BOTH', 'promo2023'), ('SYS2', 2024, 1, 'BOTH', 'promo2023'), ('BDD2', 2024, 1, 'BOTH', 'promo2023'),
    ('STAT1', 2024, 1, 'BOTH', 'promo2023'), ('ARCH1', 2024, 1, 'BOTH', 'promo2023'), ('SEC1', 2024, 1, 'BOTH', 'promo2023'),
    ('PROG4', 2024, 2, 'BOTH', 'promo2023'), ('SYS3', 2024, 2, 'BOTH', 'promo2023'), ('WEB2', 2024, 2, 'BOTH', 'promo2023'),
    ('RES2', 2024, 2, 'BOTH', 'promo2023'), ('AI1', 2024, 2, 'BOTH', 'promo2023'), ('COM2', 2024, 2, 'BOTH', 'promo2023');

  -- teacher_rakoto teaches group K1, teacher_andrianjatovo teaches K2 and TN1:
  -- the same course is taught by two different teachers to different groups.
  insert into course_assignments (id, course_id, teacher_id, group_id, year, semester)
  select 'ca_' || lower(sa.code) || '_' || sa.year || '_' || g.promotion_id || '_' || g.ref,
         'course_' || lower(sa.code) || '_id',
         case when g.ref = 'K1' then 'teacher_rakoto' else 'teacher_andrianjatovo' end,
         g.id, sa.year, sa.semester
  from seed_assignments sa
  join groups g on g.promotion_id = sa.promotion
  where (sa.track = 'BOTH' and g.path in ('EL', 'TN')) or sa.track = g.path;

  -- ============================================================ exams
  -- Two exams per course: midterm (0.4) + final (0.6), coefficients sum to 1.
  insert into exam (id, course_assignment_id, title, examination_date, coefficient)
  select 'exam_' || a.id || '_cc', a.id, 'Contrôle continu',
         make_timestamp(a.year + case when a.semester = 1 then 0 else 1 end,
                        case when a.semester = 1 then 11 else 4 end, 15, 9, 0, 0),
         0.4::decimal
  from course_assignments a
  union all
  select 'exam_' || a.id || '_final', a.id, 'Examen final',
         make_timestamp(a.year + 1, case when a.semester = 1 then 1 else 6 end, 20, 9, 0, 0),
         0.6::decimal
  from course_assignments a;

  -- ============================================================ group_flows
  -- s22_lova: 5 events (4 group changes) over the curriculum.
  -- s22_miora: switches from EL (K1) to TN (TN1) at the start of year 3.
  insert into group_flows (id, group_id, student_id, flow_type, flow_datetime) values
    ('gf_s22_lova_k1_join', 'group22_k1', 's22_lova', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_lova_k1_leave', 'group22_k1', 's22_lova', 'LEAVE', '2023-08-31 12:00:00'),
    ('gf_s22_lova_k2_join', 'group22_k2', 's22_lova', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s22_lova_k2_leave', 'group22_k2', 's22_lova', 'LEAVE', '2024-08-30 12:00:00'),
    ('gf_s22_lova_k1_rejoin', 'group22_k1', 's22_lova', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_s22_miora_k1_join', 'group22_k1', 's22_miora', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_miora_k1_leave', 'group22_k1', 's22_miora', 'LEAVE', '2024-06-30 12:00:00'),
    ('gf_s22_miora_tn1_join', 'group22_tn1', 's22_miora', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_s22_tiana_k1_join', 'group22_k1', 's22_tiana', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_toky_k2_join', 'group22_k2', 's22_toky', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_naina_k1_join', 'group22_k1', 's22_naina', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_feno_k2_join', 'group22_k2', 's22_feno', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_hery_tn1_join', 'group22_tn1', 's22_hery', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s22_sitraka_tn1_join', 'group22_tn1', 's22_sitraka', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_s23_andry_k1_join', 'group23_k1', 's23_andry', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_hasina_k1_join', 'group23_k1', 's23_hasina', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_fanja_k2_join', 'group23_k2', 's23_fanja', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_rina_k2_join', 'group23_k2', 's23_rina', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_tahiry_k2_join', 'group23_k2', 's23_tahiry', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_vonjy_k1_join', 'group23_k1', 's23_vonjy', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_zo_tn1_join', 'group23_tn1', 's23_zo', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_s23_lala_tn1_join', 'group23_tn1', 's23_lala', 'JOIN', '2023-09-01 08:00:00');

  -- ============================================================ grades
  -- Student profiles:
  --   base + jitter/2  -> deterministic score per (student, course, exam).
  --   base 11/12/13    -> passing student (every grade >= 10).
  --   base 6.5         -> failing student (every grade < 10).
  --   skip_course_code -> no final exam grade on this course (provisional transcript).
  --   skip_year        -> no final exam grade on semester 2 of this school year (provisional transcript).
  create temp table seed_profiles (
    student_id text primary key, base numeric(3, 1), jitter int,
    skip_course_code text, skip_year int
  ) on commit drop;
  insert into seed_profiles values
    ('s22_lova', 13, 7, null, null),
    ('s22_tiana', 11, 7, null, null),
    ('s22_miora', 12, 7, null, null),
    ('s22_toky', 6.5, 7, null, null),
    ('s22_naina', 11, 7, null, 2024),
    ('s22_feno', 11, 7, 'PROG6', null),
    ('s22_hery', 11, 7, null, null),
    ('s22_sitraka', 6.5, 7, null, null),
    ('s23_andry', 12, 7, null, null),
    ('s23_hasina', 11, 7, null, null),
    ('s23_fanja', 11, 7, null, null),
    ('s23_rina', 12, 7, null, null),
    ('s23_tahiry', 6.5, 7, null, null),
    ('s23_vonjy', 6.5, 7, null, null),
    ('s23_zo', 11, 7, null, 2023),
    ('s23_lala', 11, 7, 'WEB2', null);

  -- A grade is inserted for every (student, exam) pair where the student was
  -- member of the course's group at the start of the school year (last JOIN
  -- before September 1st of year + 1), so group switches are honored
  -- automatically (e.g. s22_miora follows TN courses in year 2024).
  insert into grade (id, exam_id, student_id, score, is_final)
  select 'grade_' || e.id || '_' || s.id,
         e.id,
         s.id,
         round(p.base + (abs(hashtext(s.id || ':' || a.id || ':' || e.id)::bigint) % p.jitter)::numeric / 2, 2),
         e.title = 'Examen final'
  from users s
  join seed_profiles p on p.student_id = s.id
  join course_assignments a
    on a.group_id = (
      select f.group_id
      from group_flows f
      where f.student_id = s.id
        and f.flow_type = 'JOIN'
        and f.flow_datetime < make_timestamp(a.year + 1, 9, 1, 0, 0, 0)
      order by f.flow_datetime desc
      limit 1
    )
  join courses c on c.id = a.course_id
  join exam e on e.course_assignment_id = a.id
  where s.role = 'STUDENT'
    and not (
      p.skip_course_code is not null
      and e.title = 'Examen final'
      and c.code = p.skip_course_code
    )
    and not (
      p.skip_year is not null
      and a.year = p.skip_year
      and a.semester = 2
      and e.title = 'Examen final'
    );

  -- ============================================================ grade_history
  insert into grade_history (id, grade_id, old_score, new_score, changed_at, comment)
  select 'gh_s22_lova_prog4_1', g.id, 0.0, g.score, '2025-01-22 10:30:00',
         'Première saisie après correction collective'
  from grade g
  where g.id = 'grade_exam_ca_prog4_2023_promo2022_K2_final_s22_lova';
end
$$;
