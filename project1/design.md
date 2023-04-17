## Table 1: posts

- post_id (serial, primary key)
- title (text)
- content (text)
- posting_time (timestamp)
- posting_city (text)
- category (text)
- author_id (integer, foreign key referencing authors(author_id))

## Table 2: authors

- author_id (serial, primary key)
- author_name (text)
- author_registration_time (timestamp)
- author_phone (text)

## Table 3: author_followers

- author_id (integer, foreign key referencing authors(author_id))
- follower_id (integer)

## Table 4: author_favorites

- post_id (integer, foreign key referencing posts(post_id))
- author_id (integer, foreign key referencing authors(author_id))

## Table 5: author_shared_posts

- post_id (integer, foreign key referencing posts(post_id))
- author_id (integer, foreign key referencing authors(author_id))

## Table 6: author_likes

- post_id (integer, foreign key referencing posts(post_id))
- author_id (integer, foreign key referencing authors(author_id))

## Table 7: replies

- reply_id (serial, primary key)
- post_id (integer, foreign key referencing posts(post_id))
- reply_content (text)
- reply_stars (integer)
- reply_author_id (integer, foreign key referencing authors(author_id))
- secondary_reply_content (text)
- secondary_reply_stars (integer)
- secondary_reply_author_id (integer, foreign key referencing authors(author_id))

In this revised design, the category attribute has been added to the posts table.

