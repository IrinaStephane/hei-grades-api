alter table users
    add column if not exists matricule varchar;

alter table users
    add constraint users_matricule_unique unique (matricule);

update users
set matricule = case
                    when role = 'ADMIN' and id like 'ADMIN%' then 'ADMIN' || lpad(substr(id, 6), 2, '0')
                    when id like 'STD%' or id like 'TEACH%' then id
                    else null
                end;