-- Table 4: authors (Entity set)
create table authors
(
    author_id                SERIAL,
    author_name              text      not null unique primary key,
    author_registration_time TIMESTAMP not null,
    author_phone             text,
    author_id_card           text
);

-- Table 1: posts (Entity set)
create table posts
(
    post_id     SERIAL primary key,
    title       text                                  not null,
    content     text                                  not null,
    post_time   TIMESTAMP,
    post_city   text,
    author_name text references authors (author_name) not null
);


-- Table 2: categories (Entity set)
create table categories
(
    category_id   SERIAL,
    category_name text not null unique primary key
);

-- Table 3: post_category (Relationship set)
create table post_category
(
    post_id       INTEGER references posts (post_id)         not null,
    category_name text references categories (category_name) not null,
    primary key (post_id, category_name)
);


-- Table 5: author_follow (Relationship set)
create table author_follow
(
    author_name   text references authors (author_name) not null,
    followed_name text references authors (author_name) not null,
    primary key (author_name, followed_name)
);


-- Table 6: author_favorites (Relationship set)
-- users who favorites this post
create table post_favorites
(
    post_id              INTEGER references posts (post_id)    not null,
    favorite_author_name text references authors (author_name) not null,
    primary key (post_id, favorite_author_name)
);

-- Table 7: author_shared_posts (Relationship set)
create table author_shared_posts
(
    post_id            INTEGER references posts (post_id)    not null,
    shared_author_name text references authors (author_name) not null,
    primary key (post_id, shared_author_name)
);

-- Table 8: author_liked_posts (Relationship set)
create table author_liked_posts
(
    post_id           INTEGER references posts (post_id)    not null,
    liked_author_name text references authors (author_name) not null,
    primary key (post_id, liked_author_name)
);


-- Table 9: first_replies (Entity set)
create table first_replies
(
    post_id       INTEGER references posts (post_id)    not null,
    first_id      SERIAL primary key,
    first_content text                                  not null,
    first_stars   INTEGER,
    first_author  text references authors (author_name) not null,
    unique (post_id, first_content, first_stars, first_author)
);

-- Table 10: sub_replies (Entity set)
create table second_replies
(
    first_id       INTEGER references first_replies (first_id) not null,
    second_id      SERIAL primary key,
    second_content text                                        not null,
    second_stars   INTEGER,
    second_author  text references authors (author_name)       not null
);

--------------- Basics --------------------------------------------------

select *
from authors
order by author_id desc;

select *
from authors
where author_name = 'John';

-- register a new user
-- 1.name 2.phone 3.id_card
insert into authors(author_name, author_registration_time, author_phone, author_id_card)
values ('John', current_timestamp, '123456789', '123456789');

insert into authors (author_name, author_registration_time)
values ('cooper', current_timestamp);

-- favorite a post
insert into post_favorites(post_id, favorite_author_name)
values (1, 'John');
select *
from post_favorites;
select *
from post_favorites
where favorite_author_name = 'John';

-- like a post
insert into author_liked_posts(post_id, liked_author_name)
values (1, 'John');
select *
from author_liked_posts;
select *
from author_liked_posts
where liked_author_name = 'John';

-- share a post
insert into author_shared_posts(post_id, shared_author_name)
values (1, 'John');
select *
from author_shared_posts;
select *
from author_shared_posts
where post_id = 1;

-- check user's own favorite posts list
select *
from post_favorites
where favorite_author_name = 'John';

-- check user's own liked posts list
select *
from author_liked_posts
where liked_author_name = 'John';

-- check user's own shared posts list
select *
from author_shared_posts
where shared_author_name = 'John';


-- follow a user
insert into author_follow (author_name, followed_name)
values ('John', 'financial_wife');

select *
from author_follow
where author_name = 'John';
select *
from author_follow
where followed_name = 'financial_wife';

-- unfollow a user
delete
from author_follow
where author_name = 'John'
  and followed_name = 'financial_wife';

-- check user's own followers list/ followings list
select *
from author_follow
where followed_name = 'financial_wife';
select *
from author_follow
where author_name = 'John';

-- post a new post
insert into posts(title, content, post_time, post_city, author_name)
values ('title', 'fuck sustc!', current_timestamp, 'city', 'cooper');

select *
from posts
where post_id = 203;
select *
from posts
where author_name = 'John';
select *
from first_replies
order by post_id desc;


-- reply a post (according to post_id)
insert into first_replies (post_id, first_content, first_stars, first_author)
values (1, 'helloworld', 0, 'John');
select *
from first_replies
where first_author = 'John';

-- reply a first reply
insert into second_replies (first_id, second_content, second_stars, second_author)
values (1, 'replyJohn', 0, 'John');
select *
from second_replies
where first_id = 1;

-- view user's own posts / replies
select *
from posts
where author_name = 'John';
select *
from first_replies
where first_author = 'John';
select *
from second_replies
where second_author = 'John';


-- ////////////////////////////////////////////////////////////////
-- bonus
-- anonymous replies, first create an anonymous user in authors
insert into authors (author_name, author_registration_time)
values ('anonymous', current_timestamp);
select *
from authors
where author_name = 'anonymous';

insert into first_replies (post_id, first_content, first_stars, first_author)
values (1, 'anoReply', 0, 'anonymous');

-- shield or block
-- ------------- block a user ---------------
create table blockUser
(
    author        text not null references authors (author_name),
    blockedAuthor text not null references authors (author_name),
    primary key (author, blockedAuthor)
);
alter table blockUser
    add constraint blockUser_author_fkey foreign key (author) references authors (author_name);
alter table blockUser
    add constraint blockUser_blockedAuthor_fkey foreign key (blockedAuthor) references authors (author_name);

select *
from blockUser;

-- 1. say John wants to block cooper
insert into blockUser (author, blockedAuthor)
values ('John', 'cooper');
select *
from blockUser;

-- 2. when searching, filter the result adding where.
select *
from posts -- or first_replies or second_replies
where author_name not in (select blockedAuthor from blockUser where author = 'John');

-- Hot search list function
-- 1. create a table to store the hot search list
create table hotSearchList
(
    hotSearchId    SERIAL primary key,
    search_content text not null
);

-- after selecting (search), insert the search content into the hot search list
insert into hotSearchList (search_content)
values ('sustech');

insert into hotSearchList (search_content)
values ('cooper');

insert into hotSearchList (search_content)
values ('zhong');

select *
from hotSearchList;

-- when checking the hot search list, order by the content's frequency
select search_content, count(search_content) as frequency
from hotSearchList
group by search_content
order by frequency desc;

------------- multi-parameter search ---------------
-- to be continued




