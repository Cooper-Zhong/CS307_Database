-- Table 1: authors (Entity set)
create table authors
(
    author_id                SERIAL,
    author_name              text      not null unique primary key,
    author_registration_time TIMESTAMP not null,
    author_phone             text,
    author_id_card           text
);

-- Table 2: posts (Entity set)
create table posts
(
    post_id     SERIAL primary key,
    title       text                                  not null,
    content     text                                  not null,
    post_time   TIMESTAMP,
    post_city   text,
    author_name text references authors (author_name) not null
);


-- Table 3: categories (Entity set)
create table categories
(
    category_id   SERIAL,
    category_name text not null unique primary key
);

-- Table 4: post_category (Relationship set)
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
    post_id       INTEGER references posts (post_id)       not null,
    first_id      SERIAL primary key,
    first_content text                                     not null,
    first_stars   INTEGER,
    first_author  INTEGER references authors (author_name) not null,
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


