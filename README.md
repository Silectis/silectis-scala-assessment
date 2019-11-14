# Silectis Scala Assessment

This repository contains a Scala assessment for Silectis Platform Engineer applicants

## Assessment Instructions

The purpose of this application is to accept a SQL query via an HTTP request, parse and validate 
the query, and audit which table and columns are being accessed by the query.

So far, the SQL parser has been implemented as well as the HTTP server and request handler. Your
task is to implement the query auditor that will:
 1. Resolve the referenced table and columns in the query
 2. Validate the query to ensure that all column references are valid

### SQL Parser

For this simple example, only a small subset of the SQL specification is supported. The parser only
recognizes simple `SELECT` statements with an optional `FROM` clause. If specified, the `FROM`
clause can only reference a single table (no joins). However, subqueries are supported.

Simple column references, literal values, and functions are supported as columns in the select
statement.

Aliases are also supported for both tables and columns.

For example, the following queries are supported:

```sql
select 1 as one, 'two' as two

select col1, col2 from table1

select a, b, x, y from (
  select
    col1 as a,
    2 as b,
    x,
    upper('test') as y
  from table2
) as s
```

You can find more examples of supported queries in `com.silectis.sql.parse.SqlParserSpec`.

### Query Auditor

`com.silectis.sql.audit.QueryAuditor` contains a stub of the audit method to implement. This method
should return whether the query references a table, and, if it does, which table is referenced and
which columns from that table are referenced.

For example, the following sql query
```sql
select col1, col2 from table1
```

should return
```json
{
  "table": "table1",
  "columns": ["col1", "col2"]
}
```

The following query
```sql
select a, b, x, y from (
  select
    col1 as a,
    2 as b,
    x,
    upper('test') as y
  from table2
) as s
```

should return
```json
{
  "table": "table2",
  "columns": ["col1", "x"]
}
```

And the following query
```sql
select 1 as one, 'two'
```

should return
```json
{
  "columns": []
}
```
since it does not reference any named table.

Additionally, the method should return a failure if any column in any part of the query is invalid.
For example, this query
```sql
select col1, col2 from (select col1 from test1) as s
```

should return failure since `col2` is not present in the subquery `s`.

Note that this validation is only possible for subqueries, since we do not know what columns are
present in simple tables. Therefore, you can assume that column references to simple tables are
always valid.

### Submission

Clone this repository and work on your solution, committing your changes to your local repository.
When you are finished, zip the entire project folder, including your local git repository,
and email it to `recruiting@silect.is`.

## Required Software

[SBT](https://www.scala-sbt.org/) is used as the build tool.

## Building and Testing

Start an sbt shell. We will use this to compile, test, and run the application.
```bash
sbt
```

Clean the build:
```sbtshell
clean
```

Compile and execute tests:
```sbtshell
test
```

Run the tests with code coverage and generate a coverage report:
```sbtshell
; clean; coverage; test; coverageReport
```

Run the application as server on the local machine (CTRL+C to stop):
```sbtshell
run
```

The server binds to port 8080. While it is running, you can use `curl` to submit a query for auditing:
```bash
curl --data 'select col1, col2 from test1' localhost:8080
```
