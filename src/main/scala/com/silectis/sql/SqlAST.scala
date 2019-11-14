package com.silectis.sql

/**
  * A standard SQL select statement, only supporting a list of columns from zero or one table
  */
case class Query(columns: Seq[QueryColumn],
                 from: Option[TableExpression] = None)

/**
  * An entity that can form the "from" clause of a query
  *
  * <ul>
  *   <li>Table reference, optionally with an alias</li>
  *   <li>Subquery</li>
  * </ul>
  */
sealed abstract class TableExpression

/**
  * A reference to a table, optionally qualified by a schema name
  * @param schemaName the optional schema of the table
  * @param tableName the name of the table
  */
case class TableReference(schemaName: Option[String],
                          tableName: String) extends TableExpression

/**
  * A table aliased to another name
  * @param table reference to the underlying table
  * @param alias name
  */
case class TableAlias(table: TableReference,
                      alias: String) extends TableExpression

/**
  * A nested query with an alias name
  * @param query the query
  * @param alias the name to use when referring to the query
  */
case class Subquery(query: Query, alias: String) extends TableExpression

/**
  * An expression that can be one of the columns in a select statement
  */
sealed abstract class QueryColumn

/**
  * An aliased column in the select list of a query
  * @param expr expression that defines the column
  * @param alias alias for the column
  */
case class ColumnAlias(expr: ColumnExpression,
                       alias: String) extends QueryColumn

/**
  * An entity that can be used as a column in a select list or where clause
  */
sealed abstract class ColumnExpression extends QueryColumn

/**
  * A reference to a column in a table
  * @param columnName the name of the column
  */
case class ColumnReference(columnName: String) extends ColumnExpression

/**
  * A constant value written inline in a statement
  */
sealed abstract class Literal extends ColumnExpression

case class StringLiteral(value: String) extends Literal
case class IntegerLiteral(value: Long) extends Literal
case class DecimalLiteral(value: BigDecimal) extends Literal
case class BooleanLiteral(value: Boolean) extends Literal
case object NullLiteral extends Literal

/**
  * A function applied to a set of column expressions
  * @param functionName the name of the function
  * @param arguments zero or more function arguments
  */
case class SqlFunction(functionName: String,
                       arguments: Seq[ColumnExpression])
  extends ColumnExpression
