package com.silectis.sql.parse

import com.silectis.sql._

import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.combinator.syntactical.StdTokenParsers

/**
  * Defines the grammar of our supported subset of the SQL language using the scala
  * parser combinator library.
  *
  * The text is first tokenized, then the tokens are parsed by this parser combinator.
  *
  * @author dkotsiko
  *         jlouns
  */
class SqlParser extends StdTokenParsers with PackratParsers {
  type Tokens = SqlLexical

  val lexical = new SqlLexical()

  private def parenthetical[A](parser: PackratParser[A]): PackratParser[A] = "(" ~> parser <~ ")"

  /*
   * Literals
   */

  private val stringLiteral: PackratParser[StringLiteral] =
    stringLit ^^ StringLiteral

  private val sign: PackratParser[Int] = opt("+" | "-") ^^ {
    case Some("-") => -1
    case _ => 1
  }

  private val signedLong: PackratParser[Long] =
    sign ~ numericLit ^^ {
      case s ~ i => i.toLong * s
    }

  private val integerLiteral: PackratParser[IntegerLiteral] =
    signedLong ^^ IntegerLiteral

  private val decimalLiteral: PackratParser[DecimalLiteral] =
    sign ~ numericLit ~ ("." ~> numericLit) ^^ {
      case s ~ n ~ n2 =>
        val signPrefix = if (s < 0) "-" else ""
        DecimalLiteral(BigDecimal(signPrefix + n + "." + n2))
    }

  private val nullLiteral: PackratParser[NullLiteral.type] = "null" ^^^ NullLiteral

  private val booleanLiteral: PackratParser[BooleanLiteral] =
    "true" ^^^ BooleanLiteral(true) | "false" ^^^ BooleanLiteral(false)

  // note: decimal must be attempted before integer or an error is thrown
  private val literal: PackratParser[Literal] =
    decimalLiteral | integerLiteral | booleanLiteral | stringLiteral | nullLiteral

  /*
   * Columns
   */

  private val columnReference: PackratParser[ColumnReference] =
    ident ^^ {
      c => ColumnReference(c)
    }

  private lazy val columnAlias: PackratParser[ColumnAlias] =
    columnExpression ~ (opt("as") ~> ident) ^^ {
      case expr ~ alias => ColumnAlias(expr, alias)
    }

  private lazy val selectColumn: PackratParser[QueryColumn] = columnAlias | columnExpression

  private lazy val parentheticalExpression: PackratParser[ColumnExpression] =
    parenthetical(columnExpression)

  private lazy val function: PackratParser[SqlFunction] =
    ident ~ parenthetical(repsep(columnExpression, ",")) ^^ {
      case f ~ c => SqlFunction(f, c)
    }

  private lazy val columnExpression: PackratParser[ColumnExpression] =
    literal | parentheticalExpression | function | columnReference

  /*
   * Tables
   */

  private lazy val subquery: PackratParser[Subquery] =
    parenthetical(query) ~ (opt("as") ~> ident) ^^ {
      case q ~ a => Subquery(q, a)
    }

  private val tableReference: PackratParser[TableReference] =
    opt(ident <~ ".") ~ ident ^^ {
      case s ~ t => TableReference(s, t)
    }

  private val tableAlias: PackratParser[TableAlias] =
    tableReference ~ (opt("as") ~> ident) ^^ {
      case t ~ a => TableAlias(t, a)
    }

  private lazy val tableExpression: PackratParser[TableExpression] =
    subquery | tableAlias | tableReference

  /*
   * Top level query expression
   */

  private lazy val query: PackratParser[Query] =
    "select" ~>
      rep1sep(selectColumn, ",") ~
      opt("from" ~> tableExpression) ^^ {
      case c ~ t => Query(c, t)
    }

  /**
    * Parse an input string into a SQL query expression, throwing an exception on failure.
    * @param input the sql string to parse
    * @return the parsed query expression
    */
  def parse(input: String): Query =
    phrase(query)(new lexical.Scanner(input)) match {
      case Success(res, _) => res
      case NoSuccess(msg, next) =>
        val pos = next.pos
        throw new SqlException(s"Failed at line ${pos.line}, column ${pos.column}: $msg")
    }
}
