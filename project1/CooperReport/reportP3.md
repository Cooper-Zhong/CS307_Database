## Task3

### Task3.1 Data Import

The script consists of 4 files: `dbUser.properties`,`Main`,`Post`,`Replies`.

`dbUser.properties` contains the information of database and its user, including `host`,`database`,`user`,`password`,`port`, in order to connect to the database.

`Post` is a java class to create corresponding java object from the json data. Similarly for class `Replies`.

`Main` file is used to import data. **The basic steps are as follows**:

1. Load database user information from `dbUser.properties`.
2. Connect to database using `postgresql.Driver`.
3. Clear data in relevant tables(and create relevant empty tables).
4. Load data from `posts.json` to a `List<Post> posts`.
5. Load data from `replies.json` to a `List<Replies> replies`.
6. Start the timer.
7. Prepare insert statements.
8. Traverse `posts` and `replies`, extract attributes out, set statements' parameters, add to batch.
9. Execute batch. `con.commit()` to commit changes to database.
10. Close database connection.
11. Stop the timer.

**Prerequisites**: Make sure the `dbUser.properties` is in a directory called `resources` under the project, make sure the directory `lib` contains `fastjson.jar` and `postgresql.jar` and add `lib` **as library**.

**Cautions**: Make sure that `posts.json` and `replies.json` are in the same directory as `Main`. Make sure that there are **NO space** in the attributes name in the json file.

For the script, please refer to the attachments.

### Task3.2 Efficiency Comparison

In the `Main` file, we use **`PreparedStatement`**,**`Transaction`** and **`Batch`** to improve performance and security.

In `loader1NoPrepare`, we use normal `Statement` to execute sql inserts. Since there could be `'` in an English sentence, SQL injection problem happened and data import failed.

In `loader2Prepare`, we use **`PreparedStatement`** to **precompile** the SQL statement once and then execute it multiple times with different parameter values. It helps to prevent SQL injection attacks by automatically escaping special characters in user input. Additionally, `PreparedStatement` can improve performance by **caching** the compiled SQL statement, reducing the overhead of repeatedly parsing and optimizing the statement.
On average: 4400 ms

```bash
53308 records successfully inserted.
Insertion speed: 11573 insertions/s
Time spent: 4406 ms
```

In `loader3Transaction`, we added **`Transaction`**. We start a `transaction` by disabling auto-commit mode, and then perform the database operations. If all the operations are successful, we commit the transaction. By grouping multiple operations into a single transaction, the database doesn't have to perform multiple commit operations for each individual SQL statement. It caches the changes and then `commit` to the database just **once** after all things are done.
On average: 1600 ms.

```bash
53308 records successfully inserted.
Insertion speed: 33151 insertions/s
Time spent: 1608 ms
```

In `Main`, we added **`Batch`**. It allows multiple SQL statements to be executed as a single batch, reducing the amount of network traffic between the client and the database server. With individual insertions, each insert statement requires a separate network round-trip between client-server. With `batch` insertions, multiple insert statements can be sent to the server in a single network round-trip.
On average: 620 ms.

```bash
53308 records successfully inserted.
Insertion speed: 86119 insertions/s
Time spent: 619 ms
```

Test environment: Apple MacBook Pro 2021 (M1 pro,8 cores) 16GB RAM, macOS 12.6.3. To summarize, `Batch` inserts can be useful for inserting large amounts of data, `PreparedStatement` can be useful for executing similar SQL statements multiple times, and `transactions` can be useful for ensuring data consistency.
