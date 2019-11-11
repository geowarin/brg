-- Note: Any function which meets the following conditions will be treated as a computed field by PostGraphile:
--
--     The function has a table row as the first argument.
--     The function is in the same schema as the table of the first argument.
--     The function’s name is prefixed by the table’s name.
--     The function is marked as stable or immutable which makes it a query and not a mutation.


create function forum_example.person_full_name(person forum_example.person) returns text as
$$
select person.first_name || ' ' || person.last_name
$$ language sql stable;

comment on function forum_example.person_full_name(forum_example.person) is 'A person’s full name which is a concatenation of their first and last name.';

create function forum_example.post_summary(post forum_example.post,
                                           length int default 50,
                                           omission text default '…') returns text as
$$
select case
           when post.body is null then null
           else substr(post.body, 0, length) || omission
           end
$$ language sql stable;

comment on function forum_example.post_summary(forum_example.post, int, text) is 'A truncated version of the body for summaries.';

create function forum_example.person_latest_post(person forum_example.person) returns forum_example.post as
$$
select post.*
from forum_example.post as post
where post.author_id = person.id
order by created_at desc
limit 1
$$ language sql stable;

comment on function forum_example.person_latest_post(forum_example.person) is 'Get’s the latest post written by the person.';