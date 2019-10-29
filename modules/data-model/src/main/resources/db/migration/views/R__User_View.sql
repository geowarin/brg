drop view if exists brg_security.user_view;

create view brg_security.user_view as
  select
    email,
    first_name,
    last_name,
    string_agg(ur.role::varchar, ', ') roles
  from brg_security.brg_user u
    left join brg_security.user_roles ur on u.id = ur.user_id
  group by u.id;
