## Table 1: posts (Entity set)

- post_id (serial, primary key)
- title (text)
- content (text)
- posting_time (timestamp)
- posting_city (text)
- author_id (integer, foreign key referencing authors(author_id))

## Table 2: categories (Entity set)

- category_id (serial, primary key)
- category_name (text)

## Table 3: post_category (Relationship set)

- post_id (integer, foreign key referencing posts(post_id))
- category_id (integer, foreign key referencing categories(category_id))

## Table 4: authors (Entity set)

- author_id (serial, primary key)
- author_name (text)
- author_registration_time (timestamp)
- author_phone (text)

## Table 5: author_followers (Relationship set)

- author_id (integer, foreign key referencing authors(author_id))
- follower_id (integer,integer, foreign key referencing authors(author_id))

## Table 6: author_favorites (Relationship set)

- post_id (integer, foreign key referencing posts(post_id))
- author_id (integer, foreign key referencing authors(author_id))

## Table 7: author_shared_posts (Relationship set)

- post_id (integer, foreign key referencing posts(post_id))
- author_id (integer, foreign key referencing authors(author_id))

## Table 8: author_likes (Relationship set)

- post_id (integer, foreign key referencing posts(post_id))
- author_id (integer, foreign key referencing authors(author_id))

## Table 9: first_replies (Entity set)

- first_reply_id (serial, primary key)
- post_id (integer, foreign key referencing posts(post_id))
- reply_content (text)
- reply_stars (integer)
- reply_author_id (integer, foreign key referencing authors(author_id))

## Table 10: sub_replies (Entity set)

- secondary_reply_id (serial, primary key)
- sec_reply_content (text)
- sec_reply_stars (integer)
- sec_reply_author_id (integer, foreign key referencing authors(author_id))

**Update on 4.12:**

- 1 post may have many categories, 1 category can have many posts.(many to many relationship)
- 1 post may have many replies, 1 reply can only belong to 1 post.(one to many relationship)
- 1 author may have many posts, 1 post can only belong to 1 author.(one to many relationship)
- 1 author may have many followers(also authors), 1 follower(also author) can follow many authors.(many to many relationship)
- 1 author may have many favorites, 1 favorite can only belong to 1 author.(one to many relationship)
- 1 author may have many shared posts, 1 shared post can only belong to 1 author.(one to many relationship)
- 1 author may have many likes, 1 like can only belong to 1 author.(one to many relationship)

**remove attribute "category" in table post**, add an extra table "post_category" to store the relationship between post and category.
**add an extra table "sub_replies"**, even though 1 reply corresponds to 1 secondary_reply (as in the json given), we define an extra relationship set to store the relationship between reply and secondary_reply, in case there are more than 1 secondary_reply in the future.