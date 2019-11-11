create function forum_example.search_posts(search text) returns setof forum_example.post as $$
select post.*
from forum_example.post as post
where position(search in post.headline) > 0 or position(search in post.body) > 0
$$ language sql stable;

comment on function forum_example.search_posts(text) is 'Returns posts containing a given search term.';

/*
 Note: PostGraphile will treat set returning functions as connections.
 This is what makes them so powerful for PostGraphile users. The function above would be queryable like so:
 {
  searchPosts(search: "Hello, world!", first: 5) {
    edges {
      cursor
      node {
        headline
        body
      }
    }
  }
}
 */