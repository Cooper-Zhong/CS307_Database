## Task3

### Task3.1

The script consists of 4 files: `dbUser.properties`,`Main`,`Post`,`Replies`.

`dbUser.properties` contains the information of database and its user, including `host`,`database`,`user`,`password`,`port`, in order to connect to the database.

`Post` is a java class to create corresponding java object from the json data. Similarly for class `Replies`.

`Main` file is used to import data. **The basic steps are as follows**:

1. Load database user information from `dbUser.properties`.
2. Connect to database using `postgresql.Driver`.
3. Clear data in relevant tables(and create relevant tables).
4. Load data from `posts.json` to a `List<Post> posts`.
5. Load data from `replies.json` to a `List<Replies> replies`.
6. Start the timer.
7. Prepare insert statements.
8. Traverse `posts` and `replies`, extract attributes out, set statements' parameters, add to batch.
9. Execute batch. `con.commit()` to commit changes to database.
10. Close database connection.
11. Stop the timer.

**Prerequisites**: Make sure the `dbUser.properties` is in a directory called `resources`, make sure the directory `lib` contains `fastjson.jar` and `postgresql.jar` and add `lib` **as library**.

**Cautions**: Make sure that `posts.json` and `replies.json` are in the same directory as `Main`.Make sure that there are **NO** **space** in the attributes name in the json file. **Before** executing `Main`, please create the corresponding tables in advance!

### Task3.2

In the `Main` file, we use `prepareStatement` and `Batch` to improve performance and security. 

A `PreparedStatement` object represents a **precompiled** SQL statement that can be executed multiple times with different parameters. It helps to prevent SQL injection attacks by automatically escaping special characters in user input. Additionally, `prepareStatement` can improve performance by **caching** the compiled SQL statement, reducing the overhead of repeatedly parsing and optimizing the statement.

`Batch` is a feature that allows multiple SQL statements to be executed as a single batch, reducing the number of round-trips between the Java application and the database.

Test environment: Apple MACBOOK PRO 2021 (M1 pro) 16GB RAM, macOS 12.6.3

To be continued.




