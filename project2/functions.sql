-- add
insert into block_user (author, blocked_author)
values ('lhx', 'wonderful_positive');

-- delete
delete
from block_user
where author = 'lhx'
  and blocked_author = 'coop';

select *
from block_user;

-- select posts that are not blocked by the user, only use user name
select count(*)
from posts;

select count(*)
from posts
where author_name not in (select blocked_author
                          from block_user
                          where author = 'lhx');

-- select posts that are not blocked by the user
create or replace function get_posts(my_name text)
    returns table
            (
                post_id     integer,
                title       text,
                content     text,
                post_time   timestamp,
                post_city   text,
                author_name text
            )
as
$$
begin
    return query
        select distinct p.post_id,
                        p.title,
                        p.content,
                        p.post_time,
                        p.post_city,
                        p.author_name
        from posts p
                 join post_category pc on p.post_id = pc.post_id
        where p.author_name not in (select blocked_author
                                    from block_user
                                    where author = my_name);
end;
$$ language plpgsql;

---------------------------
--function that returns posts that are not blocked by the user with first replies
---------------------------
create or replace function get_posts_first_replies(my_name text)
    returns table
            (
                post_id       integer,
                title         text,
                content       text,
                post_time     timestamp,
                post_city     text,
                category_name text,
                author_name   text,
                first_id      integer,
                first_content text,
                first_stars   integer,
                first_author  text
            )
as
$$
begin
    return query
        select distinct p.post_id,
                        p.title,
                        p.content,
                        p.post_time,
                        p.post_city,
                        pc.category_name,
                        p.author_name,
                        fr.first_id,
                        fr.first_content,
                        fr.first_stars,
                        fr.first_author
        from posts p
                 join post_category pc on p.post_id = pc.post_id
                 join first_replies fr on p.post_id = fr.post_id
        where p.author_name not in (select blocked_author
                                    from block_user
                                    where author = my_name);
end;
$$ language plpgsql;

---------------------------
--function that returns posts that are not blocked by the user with second replies
---------------------------
create or replace function get_posts_second_replies(my_name text)
    returns table
            (
                post_id        integer,
                title          text,
                content        text,
                post_time      timestamp,
                post_city      text,
                category_name  text,
                author_name    text,
                first_id       integer,
                first_content  text,
                first_stars    integer,
                first_author   text,
                second_id      integer,
                second_content text,
                second_stars   integer,
                second_author  text
            )
as
$$
begin
    return query
        select distinct p.post_id,
                        p.title,
                        p.content,
                        p.post_time,
                        p.post_city,
                        pc.category_name,
                        p.author_name,
                        fr.first_id,
                        fr.first_content,
                        fr.first_stars,
                        fr.first_author,
                        sr.second_id,
                        sr.second_content,
                        sr.second_stars,
                        sr.second_author
        from posts p
                 join post_category pc on p.post_id = pc.post_id
                 join first_replies fr on p.post_id = fr.post_id
                 join second_replies sr on fr.first_id = sr.first_id
        where p.author_name not in (select blocked_author
                                    from block_user
                                    where author = my_name);
end;
$$ language plpgsql;



-- test ---------------------------
select *
from get_posts('lhx');

select *
from get_posts_first_replies('lhx');

select *
from get_posts_second_replies('lhx');
select count(*)
from posts;



select *
from posts
where post_id not in (select post_id
                      from block_user
                      where author = 'coop');