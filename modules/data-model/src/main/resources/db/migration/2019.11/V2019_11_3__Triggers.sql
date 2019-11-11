alter table forum_example.person add column updated_at timestamp default now();
alter table forum_example.post add column updated_at timestamp default now();

create function forum_example_private.set_updated_at() returns trigger as $$
begin
    new.updated_at := current_timestamp;
    return new;
end;
$$ language plpgsql;

create trigger person_updated_at before update
    on forum_example.person
    for each row
execute procedure forum_example_private.set_updated_at();

create trigger post_updated_at before update
    on forum_example.post
    for each row
execute procedure forum_example_private.set_updated_at();
