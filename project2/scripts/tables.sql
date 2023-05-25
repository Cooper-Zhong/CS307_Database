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
create index posts_author_name_index
    on posts (author_name); -- used a lot in where clause

explain analyze select * from posts where author_name = 'coop';


-- Table 2: categories (Entity set)
create table categories
(
    category_id   SERIAL,
    category_name text not null unique primary key
);

-- Table 3: post_category (Relationship set)
create table post_category
(
    post_id       INTEGER references posts (post_id) on delete cascade         not null,
    category_name text references categories (category_name) on delete cascade not null,
    primary key (post_id, category_name)
);

-- Table 5: author_follow (Relationship set) ----------------------------
create table author_follow
(
    author_name   text references authors (author_name) not null,
    followed_name text references authors (author_name) not null,
    primary key (author_name, followed_name)
);

-- Table 6: author_favorites (Relationship set) ----------------------------
-- users who favorites this post
create table post_favorites
(
    post_id              INTEGER references posts (post_id)    not null,
    favorite_author_name text references authors (author_name) not null,
    primary key (post_id, favorite_author_name)
);
create index post_favorites_post_id_index
    on post_favorites (favorite_author_name); -- used a lot in where clause

explain analyze select * from post_favorites where favorite_author_name = 'international_welcome';

-- Table 7: author_shared_posts (Relationship set) ----------------------------
create table author_shared_posts
(
    post_id            INTEGER references posts (post_id)    not null,
    shared_author_name text references authors (author_name) not null,
    primary key (post_id, shared_author_name)
);
create index author_shared_posts_post_id_index
    on author_shared_posts (shared_author_name); -- used a lot in where clause

explain analyze select * from author_shared_posts where shared_author_name = 'international_welcome';


-- Table 8: author_liked_posts (Relationship set) ----------------------------
create table author_liked_posts
(
    post_id           INTEGER references posts (post_id)    not null,
    liked_author_name text references authors (author_name) not null,
    primary key (post_id, liked_author_name)
);
-- index
create index author_liked_posts_post_id_index
    on author_liked_posts (liked_author_name); -- used a lot in where clause

explain select * from author_liked_posts where liked_author_name = 'international_welcome';

-- Table 9: first_replies (Entity set) ----------------------------
create table first_replies
(
    post_id       INTEGER references posts (post_id) on delete cascade not null,
    first_id      SERIAL primary key,
    first_content text                                                 not null,
    first_stars   INTEGER,
    first_author  INTEGER references authors (author_name)             not null,
    unique (post_id, first_content, first_stars, first_author)
);

explain analyze
select *
from posts
         join first_replies fr on posts.post_id = fr.post_id
         join second_replies sr on fr.first_id = sr.first_id where posts.author_name = 'international_welcome';

-- Table 10: sub_replies (Entity set)
create table second_replies
(
    first_id       INTEGER references first_replies (first_id) on delete cascade not null,
    second_id      SERIAL primary key,
    second_content text                                                          not null,
    second_stars   INTEGER,
    second_author  text references authors (author_name)                         not null
);
create index second_replies_first_id_index
    on second_replies (first_id);

---------------------------- New Tables ----------------------------

-- Table 11: block_user (Relationship set)
create table block_user
(
    block_id       SERIAL,
    author         text not null references authors (author_name),
    blocked_author text not null references authors (author_name),
    primary key (author, blocked_author)
);

-- Table 12 hotSearchList (Entity set)
create table hot_search_list
(
    hot_search_id  SERIAL primary key,
    search_content text unique not null,
    frequency      INTEGER
);
--------------------------- --- tables ------------------------------


