create table brg_security.brg_user (
  id         uuid primary key      not null,
  active     boolean default true  not null,
  email      varchar(255)          not null,
  password   varchar(255)          not null,
  first_name varchar(255),
  last_name  varchar(255),

  constraint uk_user_email unique (email)
);

comment on table brg_security.brg_user
is 'User accounts on the platform';

comment on column brg_security.brg_user.active
is 'Is the user active. Inactive users cannot login';

comment on column brg_security.brg_user.email
is 'The user email address. Should be unique';

comment on column brg_security.brg_user.password
is 'Encoded user password';

CREATE TYPE brg_security.role AS ENUM ('admin', 'user');

create table brg_security.user_roles (
  user_id uuid not null,
  role    brg_security.role,

  constraint fk_users_role_user foreign key (user_id) references brg_security.brg_user (id)
);
