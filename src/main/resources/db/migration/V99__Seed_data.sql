-- Demo seed data mirroring the real HEI organisation:
--   - promotions are named by group letter: promG (entered 2022), promJ (entered 2023),
--     promK (entered 2024, current L1).
--   - L1 has 5 groups (G1-G5 / J1-J5 / K1-K5), all on the common track.
--   - L2 S3 keeps 3 groups (G1-G3 / J1-J3 / K1-K3): groups 4 and 5 are dissolved and
--     their students are spread over the 3 remaining groups.
--   - L2 S4 introduces the track choice: EL (Ecosysteme Logiciel) in groups 1-2,
--     TN (Transformation Numerique) in group 3. Courses diverge from S4 on.
--   - L3 keeps the same 3-group / 2-track layout.
-- Passwords: all users share the BCrypt hash of "password123"
do
$$
declare
  password_hash constant text := '$2y$10$vdZj4foBzTNEo18/LjlCOuAMhVDcelp9eZ1.FxLnFGZxS4y1McJfy';
begin
  -- ============================================================ users
  insert into users (id, first_name, last_name, email, password_hash, role, created_at) values
    ('ADMIN1', 'Admin', 'HEI', 'admin@hei.school', password_hash, 'ADMIN', '2022-08-15 09:00:00'),
    ('TEACH01', 'Mamy', 'Rakotomalala', 'mamy.rakotomalala@hei.school', password_hash, 'TEACHER', '2022-08-20 09:00:00'),
    ('TEACH02', 'Fara', 'Andrianjatovo', 'fara.andrianjatovo@hei.school', password_hash, 'TEACHER', '2022-08-20 09:00:00'),
    -- promotion G (2022) - 3rd year in 2024-2025, graduates in 2025
    ('STD22001', 'Lova', 'Randrianarisoa', 'lova.randrianarisoa@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22002', 'Tiana', 'Rakotomalala', 'tiana.rakotomalala@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22003', 'Miora', 'Andrianjaka', 'miora.andrianjaka@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22004', 'Toky', 'Rasoanaivo', 'toky.rasoanaivo@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22005', 'Naina', 'Andriamasinoro', 'naina.andriamasinoro@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22006', 'Feno', 'Rabeantoandro', 'feno.rabeantoandro@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22007', 'Hery', 'Ramanantsoa', 'hery.ramanantsoa@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    ('STD22008', 'Sitraka', 'Randriamampionona', 'sitraka.randriamampionona@hei.school', password_hash, 'STUDENT', '2022-09-01 08:00:00'),
    -- promotion J (2023) - 2nd year in 2024-2025, chooses its track at L2 S4
    ('STD23001', 'Andry', 'Ravoahangy', 'andry.ravoahangy@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23002', 'Hasina', 'Rakotoniaina', 'hasina.rakotoniaina@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23003', 'Fanja', 'Ramananandrasana', 'fanja.ramananandrasana@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23004', 'Rina', 'Andrianarivelo', 'rina.andrianarivelo@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23005', 'Tahiry', 'Razafindrakoto', 'tahiry.razafindrakoto@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23006', 'Vonjy', 'Randriambololona', 'vonjy.randriambololona@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23007', 'Zo', 'Rasolofoniaina', 'zo.rasolofoniaina@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    ('STD23008', 'Lala', 'Rakotondrazaka', 'lala.rakotondrazaka@hei.school', password_hash, 'STUDENT', '2023-09-01 08:00:00'),
    -- promotion K (2024) - 1st year in 2024-2025, currently spread over K1-K5
    ('STD24001', 'Mialy', 'Rasoanaivo', 'mialy.rasoanaivo@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24002', 'Anto', 'Raveloson', 'anto.raveloson@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24003', 'Bema', 'Randrianarison', 'bema.randrianarison@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24004', 'Cecil', 'Rakotomalala', 'cecil.rakotomalala@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24005', 'Dina', 'Andriamihaja', 'dina.andriamihaja@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24006', 'Ela', 'Razafintsalama', 'ela.razafintsalama@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24007', 'Fetra', 'Rakotondrabe', 'fetra.rakotondrabe@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00'),
    ('STD24008', 'Gaby', 'Andrianjafy', 'gaby.andrianjafy@hei.school', password_hash, 'STUDENT', '2024-09-01 08:00:00');

  -- ============================================================ promotions
  insert into promotions (id, ref, entry_year) values
    ('promG', 'G', 2022),
    ('promJ', 'J', 2023),
    ('promK', 'K', 2024);

  -- ============================================================ groups
  -- 5 groups in L1; groups 4 and 5 are dissolved at the start of L2 (their students
  -- are spread over groups 1-3). The EL/TN path only matters from L2 S4 onward:
  -- groups 1-2 host EL, group 3 hosts TN.
  insert into groups (id, ref, path, promotion_id) values
    ('groupG1', 'G1', 'EL', 'promG'),
    ('groupG2', 'G2', 'EL', 'promG'),
    ('groupG3', 'G3', 'TN', 'promG'),
    ('groupG4', 'G4', 'TN', 'promG'),
    ('groupG5', 'G5', 'TN', 'promG'),
    ('groupJ1', 'J1', 'EL', 'promJ'),
    ('groupJ2', 'J2', 'EL', 'promJ'),
    ('groupJ3', 'J3', 'TN', 'promJ'),
    ('groupJ4', 'J4', 'TN', 'promJ'),
    ('groupJ5', 'J5', 'TN', 'promJ'),
    ('groupK1', 'K1', 'EL', 'promK'),
    ('groupK2', 'K2', 'EL', 'promK'),
    ('groupK3', 'K3', 'TN', 'promK'),
    ('groupK4', 'K4', 'TN', 'promK'),
    ('groupK5', 'K5', 'TN', 'promK');

  -- ============================================================ courses
  -- Real HEI L1/L2 S3 courses (from the actual transcript), 60 credits in L1 and L2 S3.
  -- L2 S4 splits EL (Ecosysteme Logiciel) vs TN (Transformation Numerique), then L3 keeps
  -- a common core plus the two tracks. 180 credits over the 3-year curriculum.
  create temp table seed_courses (code text primary key, title text, credits int) on commit drop;
  insert into seed_courses values
    -- L1 (60 credits)
    ('PROG1', 'Algorithmique', 6),
    ('PROG2', 'Implementation d''API backend - Programmation orientee objet', 10),
    ('WEB1', 'Interface web', 6),
    ('WEB2', 'Applications web globalement connectees', 8),
    ('SYS1', 'Systemes d''exploitation', 6),
    ('SYS2', 'Systemes interconnectes', 8),
    ('LV1', 'Francais - Methodologie universitaire', 4),
    ('MGT1', 'Travail collaboratif', 4),
    ('THEORIE1', 'Mathematiques appliquees a l''informatique', 4),
    ('DONNEES1', 'Bases de donnees structurees', 4),
    -- L2 S3, common track (30 credits)
    ('MGT2', 'Gestion de projet', 5),
    ('PRO1', 'Vie professionnelle', 3),
    ('LV2', 'Anglais', 4),
    ('WEB3', 'Applications web globalement connectees - Avance', 8),
    ('PROG3', 'Implementation d''API backend - suite', 6),
    ('SYS3', 'Systemes III', 4),
    -- L2 S4, EL track (30 credits)
    ('PROG4', 'Qualite et surete des applications', 8),
    ('ARCH1', 'Architecture logicielle', 6),
    ('AI1', 'Intelligence artificielle I', 6),
    ('WEB4', 'Applications web avancees', 4),
    ('STAT1', 'Statistiques', 4),
    ('MGT3', 'Gestion de projet avancee', 2),
    -- L2 S4, TN track (30 credits)
    ('TN1', 'Transformation numerique I', 10),
    ('TN2', 'Transformation numerique II', 10),
    ('METIER1', 'Metiers de la transformation numerique', 10),
    -- L3 common core (20 credits)
    ('AI2', 'Intelligence artificielle II', 4),
    ('SEC2', 'Securite II', 4),
    ('MEM1', 'Memoire I', 4),
    ('MEM2', 'Memoire II', 4),
    ('RES3', 'Reseaux III', 2),
    ('COM3', 'Communication III', 2),
    -- L3 EL track (40 credits)
    ('PROG5', 'Programmation avancee', 8),
    ('PROG6', 'Programmation des systemes', 8),
    ('LOG1', 'Logique', 8),
    ('LOG2', 'Logique II', 6),
    ('AI3', 'Intelligence artificielle III', 4),
    ('SEC3', 'Securite III', 6),
    -- L3 TN track (40 credits)
    ('TN3', 'Transformation numerique III', 10),
    ('TN4', 'Transformation numerique IV', 8),
    ('METIER2', 'Metiers de la transformation numerique II', 8),
    ('IOT1', 'Internet des objets I', 8),
    ('TRA1', 'Telecommunications I', 6);

  insert into courses (id, code, title, credits)
  select 'course_' || lower(code) || '_id', code, title, credits from seed_courses;

  -- ============================================================ course_assignments
  -- track = 'BOTH' (common track: all groups), 'EL' (EL groups), 'TN' (TN group).
  -- promotion = the promotion whose groups take this course that year.
  create temp table seed_assignments (code text, year int, semester int, track text, promotion text) on commit drop;
  insert into seed_assignments values
    -- L1 for promotion G (2022)
    ('PROG2', 2022, 1, 'BOTH', 'promG'), ('PROG1', 2022, 1, 'BOTH', 'promG'), ('WEB1', 2022, 1, 'BOTH', 'promG'),
    ('LV1', 2022, 1, 'BOTH', 'promG'), ('MGT1', 2022, 1, 'BOTH', 'promG'),
    ('WEB2', 2022, 2, 'BOTH', 'promG'), ('SYS2', 2022, 2, 'BOTH', 'promG'), ('SYS1', 2022, 2, 'BOTH', 'promG'),
    ('THEORIE1', 2022, 2, 'BOTH', 'promG'), ('DONNEES1', 2022, 2, 'BOTH', 'promG'),
    -- L2 for promotion G (2023): S3 common, S4 split
    ('MGT2', 2023, 1, 'BOTH', 'promG'), ('PRO1', 2023, 1, 'BOTH', 'promG'), ('LV2', 2023, 1, 'BOTH', 'promG'),
    ('WEB3', 2023, 1, 'BOTH', 'promG'), ('PROG3', 2023, 1, 'BOTH', 'promG'), ('SYS3', 2023, 1, 'BOTH', 'promG'),
    ('PROG4', 2023, 2, 'EL', 'promG'), ('ARCH1', 2023, 2, 'EL', 'promG'), ('AI1', 2023, 2, 'EL', 'promG'),
    ('WEB4', 2023, 2, 'EL', 'promG'), ('STAT1', 2023, 2, 'EL', 'promG'), ('MGT3', 2023, 2, 'EL', 'promG'),
    ('TN1', 2023, 2, 'TN', 'promG'), ('TN2', 2023, 2, 'TN', 'promG'), ('METIER1', 2023, 2, 'TN', 'promG'),
    -- L3 for promotion G (2024): common core + EL/TN tracks
    ('MEM1', 2024, 1, 'BOTH', 'promG'), ('AI2', 2024, 1, 'BOTH', 'promG'), ('SEC2', 2024, 1, 'BOTH', 'promG'),
    ('RES3', 2024, 1, 'BOTH', 'promG'),
    ('PROG5', 2024, 1, 'EL', 'promG'), ('LOG1', 2024, 1, 'EL', 'promG'),
    ('TN3', 2024, 1, 'TN', 'promG'), ('TRA1', 2024, 1, 'TN', 'promG'),
    ('MEM2', 2024, 2, 'BOTH', 'promG'), ('COM3', 2024, 2, 'BOTH', 'promG'),
    ('PROG6', 2024, 2, 'EL', 'promG'), ('LOG2', 2024, 2, 'EL', 'promG'), ('AI3', 2024, 2, 'EL', 'promG'), ('SEC3', 2024, 2, 'EL', 'promG'),
    ('TN4', 2024, 2, 'TN', 'promG'), ('METIER2', 2024, 2, 'TN', 'promG'), ('IOT1', 2024, 2, 'TN', 'promG'),
    -- L1 for promotion J (2023)
    ('PROG2', 2023, 1, 'BOTH', 'promJ'), ('PROG1', 2023, 1, 'BOTH', 'promJ'), ('WEB1', 2023, 1, 'BOTH', 'promJ'),
    ('LV1', 2023, 1, 'BOTH', 'promJ'), ('MGT1', 2023, 1, 'BOTH', 'promJ'),
    ('WEB2', 2023, 2, 'BOTH', 'promJ'), ('SYS2', 2023, 2, 'BOTH', 'promJ'), ('SYS1', 2023, 2, 'BOTH', 'promJ'),
    ('THEORIE1', 2023, 2, 'BOTH', 'promJ'), ('DONNEES1', 2023, 2, 'BOTH', 'promJ'),
    -- L2 for promotion J (2024): S3 common, S4 split
    ('MGT2', 2024, 1, 'BOTH', 'promJ'), ('PRO1', 2024, 1, 'BOTH', 'promJ'), ('LV2', 2024, 1, 'BOTH', 'promJ'),
    ('WEB3', 2024, 1, 'BOTH', 'promJ'), ('PROG3', 2024, 1, 'BOTH', 'promJ'), ('SYS3', 2024, 1, 'BOTH', 'promJ'),
    ('PROG4', 2024, 2, 'EL', 'promJ'), ('ARCH1', 2024, 2, 'EL', 'promJ'), ('AI1', 2024, 2, 'EL', 'promJ'),
    ('WEB4', 2024, 2, 'EL', 'promJ'), ('STAT1', 2024, 2, 'EL', 'promJ'), ('MGT3', 2024, 2, 'EL', 'promJ'),
    ('TN1', 2024, 2, 'TN', 'promJ'), ('TN2', 2024, 2, 'TN', 'promJ'), ('METIER1', 2024, 2, 'TN', 'promJ'),
    -- L1 for promotion K (2024)
    ('PROG2', 2024, 1, 'BOTH', 'promK'), ('PROG1', 2024, 1, 'BOTH', 'promK'), ('WEB1', 2024, 1, 'BOTH', 'promK'),
    ('LV1', 2024, 1, 'BOTH', 'promK'), ('MGT1', 2024, 1, 'BOTH', 'promK'),
    ('WEB2', 2024, 2, 'BOTH', 'promK'), ('SYS2', 2024, 2, 'BOTH', 'promK'), ('SYS1', 2024, 2, 'BOTH', 'promK'),
    ('THEORIE1', 2024, 2, 'BOTH', 'promK'), ('DONNEES1', 2024, 2, 'BOTH', 'promK');

  -- TEACH01 teaches the EL groups, TEACH02 the TN groups:
  -- the same common-track course is taught by two different teachers to different groups.
  insert into course_assignments (id, course_id, teacher_id, group_id, year, semester)
  select 'ca_' || lower(sa.code) || '_' || sa.year || '_' || g.promotion_id || '_' || g.ref,
         'course_' || lower(sa.code) || '_id',
         case when g.path = 'EL' then 'TEACH01' else 'TEACH02' end,
         g.id, sa.year, sa.semester
  from seed_assignments sa
  join groups g on g.promotion_id = sa.promotion
  where (sa.track = 'BOTH' and g.path in ('EL', 'TN')) or sa.track = g.path;

  -- ============================================================ exams
  -- Two exams per course: midterm (0.4) + final (0.6), coefficients sum to 1.
  insert into exam (id, course_assignment_id, title, examination_date, coefficient)
  select 'exam_' || a.id || '_cc', a.id, 'Controle continu',
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
  -- promotion G: L1 (2022) spread over G1-G5, G4/G5 dissolved at L2 (2023),
  --   L3 (2024) split into EL (G1/G2) and TN (G3).
  --   STD22001: G1 -> G2 -> G1 (3 changes), archetype of the K1 -> K2(EL) -> K1 path.
  --   STD22003: switches from EL (G1) to TN (G3) at the start of year 3.
  -- promotion J: L1 (2023) over J1-J5, dissolution at L2 (2024),
  --   mid-year track choice at L2 S4 (2025-02).
  -- promotion K: L1 (2024) over K1-K5, everyone on the common track.
  insert into group_flows (id, group_id, student_id, flow_type, flow_datetime) values
    ('gf_STD22001_g1_join', 'groupG1', 'STD22001', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22001_g1_leave', 'groupG1', 'STD22001', 'LEAVE', '2023-08-31 12:00:00'),
    ('gf_STD22001_g2_join', 'groupG2', 'STD22001', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD22001_g2_leave', 'groupG2', 'STD22001', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD22001_g1_rejoin', 'groupG1', 'STD22001', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD22002_g5_join', 'groupG5', 'STD22002', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22002_g5_leave', 'groupG5', 'STD22002', 'LEAVE', '2023-08-31 12:00:00'),
    ('gf_STD22002_g1_join', 'groupG1', 'STD22002', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD22002_g1_leave', 'groupG1', 'STD22002', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD22002_g2_join', 'groupG2', 'STD22002', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD22003_g1_join', 'groupG1', 'STD22003', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22003_g1_leave', 'groupG1', 'STD22003', 'LEAVE', '2024-06-30 12:00:00'),
    ('gf_STD22003_g3_join', 'groupG3', 'STD22003', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD22004_g2_join', 'groupG2', 'STD22004', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22004_g2_leave', 'groupG2', 'STD22004', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD22004_g1_join', 'groupG1', 'STD22004', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD22005_g4_join', 'groupG4', 'STD22005', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22005_g4_leave', 'groupG4', 'STD22005', 'LEAVE', '2023-08-31 12:00:00'),
    ('gf_STD22005_g2_join', 'groupG2', 'STD22005', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD22005_g2_leave', 'groupG2', 'STD22005', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD22005_g1_join', 'groupG1', 'STD22005', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD22006_g2_join', 'groupG2', 'STD22006', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22006_g2_leave', 'groupG2', 'STD22006', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD22006_g1_join', 'groupG1', 'STD22006', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD22007_g3_join', 'groupG3', 'STD22007', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22008_g4_join', 'groupG4', 'STD22008', 'JOIN', '2022-09-01 08:00:00'),
    ('gf_STD22008_g4_leave', 'groupG4', 'STD22008', 'LEAVE', '2023-08-31 12:00:00'),
    ('gf_STD22008_g3_join', 'groupG3', 'STD22008', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23001_j1_join', 'groupJ1', 'STD23001', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23001_j1_leave', 'groupJ1', 'STD23001', 'LEAVE', '2025-01-31 12:00:00'),
    ('gf_STD23001_j2_join', 'groupJ2', 'STD23001', 'JOIN', '2025-02-01 08:00:00'),
    ('gf_STD23002_j1_join', 'groupJ1', 'STD23002', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23003_j2_join', 'groupJ2', 'STD23003', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23004_j2_join', 'groupJ2', 'STD23004', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23005_j2_join', 'groupJ2', 'STD23005', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23005_j2_leave', 'groupJ2', 'STD23005', 'LEAVE', '2025-01-31 12:00:00'),
    ('gf_STD23005_j3_join', 'groupJ3', 'STD23005', 'JOIN', '2025-02-01 08:00:00'),
    ('gf_STD23006_j3_join', 'groupJ3', 'STD23006', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23007_j4_join', 'groupJ4', 'STD23007', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23007_j4_leave', 'groupJ4', 'STD23007', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD23007_j3_join', 'groupJ3', 'STD23007', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD23008_j5_join', 'groupJ5', 'STD23008', 'JOIN', '2023-09-01 08:00:00'),
    ('gf_STD23008_j5_leave', 'groupJ5', 'STD23008', 'LEAVE', '2024-08-31 12:00:00'),
    ('gf_STD23008_j1_join', 'groupJ1', 'STD23008', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD23008_j1_leave', 'groupJ1', 'STD23008', 'LEAVE', '2025-01-31 12:00:00'),
    ('gf_STD23008_j2_join', 'groupJ2', 'STD23008', 'JOIN', '2025-02-01 08:00:00'),
    ('gf_STD24001_k1_join', 'groupK1', 'STD24001', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24002_k1_join', 'groupK1', 'STD24002', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24003_k2_join', 'groupK2', 'STD24003', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24004_k2_join', 'groupK2', 'STD24004', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24005_k3_join', 'groupK3', 'STD24005', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24006_k4_join', 'groupK4', 'STD24006', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24007_k4_join', 'groupK4', 'STD24007', 'JOIN', '2024-09-01 08:00:00'),
    ('gf_STD24008_k5_join', 'groupK5', 'STD24008', 'JOIN', '2024-09-01 08:00:00');

  -- ============================================================ grades
  -- Student profiles:
  --   base + jitter/2  -> deterministic score per (student, course, exam).
  --   base 11/12/13    -> passing student (every grade >= 10).
  --   base 6.5         -> failing student (every grade < 10).
  --   skip_course_code -> no final exam grade on this course (provisional transcript).
  --   skip_year        -> no final exam grade on semester 2 of this school year
  --                       (provisional transcript, e.g. STD23007: S4 grades "not started yet").
  create temp table seed_profiles (
    student_id text primary key, base numeric(3, 1), jitter int,
    skip_course_code text, skip_year int
  ) on commit drop;
  insert into seed_profiles values
    ('STD22001', 13, 7, null, null),
    ('STD22002', 11, 7, null, null),
    ('STD22003', 12, 7, null, null),
    ('STD22004', 11, 7, null, null),
    ('STD22005', 11, 7, null, 2024),
    ('STD22006', 11, 7, 'PROG6', null),
    ('STD22007', 11, 7, null, null),
    ('STD22008', 6.5, 7, null, null),
    ('STD23001', 12, 7, null, null),
    ('STD23002', 11, 7, null, null),
    ('STD23003', 11, 7, null, null),
    ('STD23004', 12, 7, null, null),
    ('STD23005', 6.5, 7, null, null),
    ('STD23006', 11, 7, null, null),
    ('STD23007', 11, 7, null, 2024),
    ('STD23008', 11, 7, 'WEB2', null),
    ('STD24001', 12, 7, null, null),
    ('STD24002', 12, 7, null, null),
    ('STD24003', 11, 7, null, null),
    ('STD24004', 11, 7, 'WEB1', null),
    ('STD24005', 13, 7, null, null),
    ('STD24006', 12, 7, null, null),
    ('STD24007', 11, 7, null, null),
    ('STD24008', 11, 7, null, null);

  -- A grade is inserted for every (student, exam) pair where the student was
  -- member of the course's group at the start of the school year (last JOIN
  -- before September 1st of year + 1), so group switches and the L2 S4 track
  -- choice are honored automatically (e.g. STD22003 follows TN courses in
  -- year 2024, STD23001 follows EL courses from the 2025 mid-year choice).
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
  select 'gh_STD22001_prog4_1', g.id, 0.0, g.score, '2025-01-22 10:30:00',
         'Premiere saisie apres correction collective'
  from grade g
  where g.id = 'grade_exam_ca_prog4_2023_promG_G2_final_STD22001';
end
$$;