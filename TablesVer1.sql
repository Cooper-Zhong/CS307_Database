-- Table 4: authors (Entity set)
create table authors
(
    author_id                SERIAL,
    author_name              text not null unique primary key,
    author_registration_time TIMESTAMP,
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
    category_id   SERIAL primary key,
    category_name text not null unique
);

-- Table 3: post_category (Relationship set)
create table post_category
(
    post_id     INTEGER references posts (post_id),
    category_id INTEGER references categories (category_id),
    primary key (post_id, category_id)
);



-- Table 5: author_followers (Relationship set)
create table author_followers
(
    author_name   text references authors (author_name),
--     follower_id INTEGER references authors (author_id),
    follower_name text references authors (author_name) not null,
    primary key (author_name, follower_name)
);


-- Table 6: author_favorites (Relationship set)
-- users who favorites this post
create table post_favorites
(
    post_id   INTEGER references posts (post_id),
    favorite_author_name text references authors (author_name) not null,
    primary key (post_id, favorite_author_name)
);

-- Table 7: author_shared_posts (Relationship set)
create table author_shared_posts
(
    post_id   INTEGER references posts (post_id),
    shared_author_name text references authors (author_name) not null,
    primary key (post_id, shared_author_name)
);

-- Table 8: author_liked_posts (Relationship set)
create table author_liked_posts
(
    post_id   INTEGER references posts (post_id),
    liked_author_name text references authors (author_name) not null,
    primary key (post_id, liked_author_name)
);

-- Table 9: first_replies (Entity set)
create table first_replies
(
    first_reply_id  SERIAL primary key,
    post_id         INTEGER references posts (post_id),
    reply_content   text                                   not null,
    reply_stars     INTEGER,
    reply_author_id INTEGER references authors (author_id) not null
);

-- Table 10: sub_replies (Entity set)
create table sub_replies
(
    secondary_reply_id  SERIAL primary key,
    sec_reply_content   text                                   not null,
    sec_reply_stars     INTEGER,
    sec_reply_author_id INTEGER references authors (author_id) not null
);

-- Table 11: reply_secondary_reply (Relationship set)
create table reply_secondary_reply
(
    first_reply_id     INTEGER references first_replies (first_reply_id),
    secondary_reply_id INTEGER references sub_replies (secondary_reply_id),
    primary key (first_reply_id, secondary_reply_id)
);

