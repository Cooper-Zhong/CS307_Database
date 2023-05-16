------------
-- function that returns posts that are not blocked by the user
------------
create or replace function get_posts(my_name text)
    returns table
            (
                post_id       integer,
                title         text,
                content       text,
                post_time     timestamp,
                post_city     text,
                author_name   text,
                category_name text
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
                        p.author_name,
                        pc.category_name
        from posts p
                 join post_category pc on p.post_id = pc.post_id
        where p.author_name not in (select blocked_author
                                    from block_user
                                    where author = my_name);--posts that are not blocked by the user
end;
$$ language plpgsql;

select *
from get_posts('cooper');
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
                 left join first_replies fr on p.post_id = fr.post_id
             -- left join to make sure that posts without first replies can also be selected
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
                 left join first_replies fr on p.post_id = fr.post_id
                 left join second_replies sr on fr.first_id = sr.first_id
             -- left join to make sure that posts without first/second replies can also be selected.
        where p.author_name not in (select blocked_author
                                    from block_user
                                    where author = my_name);
end;
$$ language plpgsql;


-- test ---------------------------
select *
from get_posts('lhx');

select *
from get_posts_first_replies('anonymous');

select *
from get_posts_second_replies('anonymous');

-- create an anonymous user in authors
insert into authors (author_name, author_registration_time)
values ('anonymous', current_timestamp);

--- hot search list ---------------------------
-- View
create view get_hot_search_list as
select *
from hot_search_list
order by frequency desc
limit 20;


--- update hot search list ---------------------------
create or replace function update_hot_search_list(content text)
    returns void
as
$$
begin
    if exists(select *
              from hot_search_list hsl
              where hsl.search_content = content) then
        update hot_search_list hsl
        set frequency = frequency + 1
        where hsl.search_content = content;
    else
        insert into hot_search_list (search_content, frequency)
        values (content, 1);
    end if;
end;
$$ language plpgsql;

---- test ---------------------------
select *
from get_hot_search_list;


--- show my liked posts ---------------------------
create or replace function show_liked_posts(my_name text)
    returns table
            (
                post_id     integer,
                author_name text,
                title       text,
                content     text,
                post_time   timestamp,
                post_city   text
            )
as
$$
begin
    return query
        select distinct p.post_id,
                        p.author_name,
                        p.title,
                        p.content,
                        p.post_time,
                        p.post_city
        from posts p
                 join author_liked_posts alp on p.post_id = alp.post_id
        where alp.liked_author_name = my_name;
end;
$$ language plpgsql;

select *
from show_liked_posts('cooperer');

--- show my favorite posts ---------------------------
create or replace function show_favorite_posts(my_name text)
    returns table
            (
                post_id     integer,
                author_name text,
                title       text,
                content     text,
                post_time   timestamp,
                post_city   text
            )
as
$$
begin
    return query
        select p.post_id, p.author_name, p.title, p.content, p.post_time, p.post_city
        from posts p
                 join post_favorites pf on p.post_id = pf.post_id
        where pf.favorite_author_name = my_name;
end;
$$ language plpgsql;

select *
from show_favorite_posts('lhx');

-- show my shared posts ---------------------------
create or replace function show_shared_posts(my_name text)
    returns table
            (
                post_id     integer,
                author_name text,
                title       text,
                content     text,
                post_time   timestamp,
                post_city   text
            )
as
$$
begin
    return query
        select p.post_id, p.author_name, p.title, p.content, p.post_time, p.post_city
        from posts p
                 join author_shared_posts asp on p.post_id = asp.post_id
        where asp.shared_author_name = my_name;
end;
$$ language plpgsql;

select *
from show_shared_posts('cooper');


-- show my following authors ---------------------------
create or replace function show_following_list(my_name text)
    returns table
            (
                followed_name text
            )
as
$$
begin
    return query
        select af.followed_name
        from author_follow af
        where af.author_name = my_name;
end;
$$ language plpgsql;

select *
from show_following_list('cooper');

-- show my blocked authors ---------------------------
create or replace function show_blocked_list(my_name text)
    returns table
            (
                blocked_author text
            )
as
$$
begin
    return query
        select bu.blocked_author
        from block_user bu
        where bu.author = my_name;
end;
$$ language plpgsql;

select *
from show_blocked_list('cooper');

-- show my posts ---------------------------
create or replace function show_my_posts(my_name text)
    returns table
            (
                post_id     integer,
                author_name text,
                title       text,
                content     text,
                post_time   timestamp,
                post_city   text
            )
as
$$
begin
    return query
        select p.post_id, p.author_name, p.title, p.content, p.post_time, p.post_city
        from posts p
        where p.author_name = my_name
        order by p.post_id asc;
end;
$$ language plpgsql;

select *
from show_my_posts('cooper');

-- show my replies ---------------------------
create or replace function show_my_replies(my_name text)
    returns table
            (
                post_id        integer,
                author_name    text,
                title          text,
                content        text,
                post_time      timestamp,
                post_city      text,
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
        select p.post_id,
               p.author_name,
               p.title,
               p.content,
               p.post_time,
               p.post_city,
               fr.first_id,
               fr.first_content,
               fr.first_stars,
               fr.first_author,
               sr.second_id,
               sr.second_content,
               sr.second_stars,
               sr.second_author
        from posts p
                 join first_replies fr on p.post_id = fr.post_id
                 left join second_replies sr on fr.first_id = sr.first_id
        where fr.first_author = my_name
           or sr.second_author = my_name
        order by p.post_id asc;
end;
$$ language plpgsql;

drop function show_my_replies(text);

select *
from show_my_replies('cooper');

---- create a new post ---------------------------

create or replace function create_post(my_name text, ntitle text, ncontent text, npost_city text)
    returns integer
as
$$
begin
    insert into posts (author_name, title, content, post_time, post_city)
    values (my_name, ntitle, ncontent, now()::timestamp(0), npost_city);
    return currval('posts_post_id_seq');

end;
$$ language plpgsql;

--- update category and post_category when creating post---
create or replace function update_category_and_post_category(new_post_id integer, ncategory text[])
    returns void
as
$$
begin
    for i in 1..array_length(ncategory, 1)
        loop
            insert into categories (category_name)
            values (ncategory[i])
            on duplicate key update nothing; -- openGauss grammar
            insert into post_category (post_id, category_name)
            values (new_post_id, ncategory[i]);
        end loop;
end;
$$ language plpgsql;

---- delete a post ---------------------------
create or replace function delete_post(del_post_id integer, my_name text)
    returns integer
as
$$
declare
begin
    -- check if the post is created by the user
    if not is_my_post(my_name, del_post_id) then
        raise exception 'You can only delete your own post!';
    end if;
    -- delete the post category and replies
    perform delete_post_category_and_replies(del_post_id);
    -- delete the post
    delete from posts p where p.author_name = my_name and p.post_id = del_post_id;
    return 1; -- success
end;
$$ language plpgsql;

drop function delete_post(integer, text);
select *
from post_category
where post_id > 204;

-- function to delete the post category and replies
create or replace function delete_post_category_and_replies(del_id integer)
    returns void
as
$$
declare
    temp_first_id integer[];
begin
    -- delete the post category
    delete
    from post_category pc
    where pc.post_id = del_id;

    select array(select fr.first_id
                 from first_replies fr
                 where fr.post_id = del_id)
    into temp_first_id;
    -- if no first replies, no need to delete replies
    if temp_first_id is not null then
        if array_length(temp_first_id, 1) is not null then
            -- delete the second replies, if there is any.
            for i in array_lower(temp_first_id, 1) .. array_upper(temp_first_id, 1)
                loop
                    delete
                    from second_replies sr
                    where sr.first_id = temp_first_id[i];
                end loop;

            -- delete the first replies
            delete
            from first_replies fr
            where fr.post_id = del_id;
        end if;
    else
        raise exception 'Null array';
    end if;
end;
$$ language plpgsql;


--- is my post? ---------------------------
create or replace function is_my_post(my_name text, del_id integer)
    returns boolean
as
$$
begin
    return exists(select *
                  from posts p
                  where p.author_name = my_name
                    and p.post_id = del_id);
end;
$$ language plpgsql;

select is_my_post('hoyin', 206);

select array(select first_id
             from first_replies fr
             where fr.post_id = 3
           );




