package com.silectis.sql.parse

import scala.annotation.tailrec
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.input.CharArrayReader.EofCh

/**
  * The lexical parser for our supported subset of the SQL language. This parser defines
  * the logic to split input into a set of tokens that will be processed by the grammar.
  */
class SqlLexical extends StdLexical {
  reserved += ("select", "from", "as", "true", "false", "null")

  delimiters += ("+", "-", ",", "(", ")", ".")

  private val identifier: Parser[String] =
    identChar ~ rep(identChar | digit) ^^ {
      case first ~ rest =>
        (first :: rest)
          .mkString("")
          .toLowerCase
    }

  private val numericLit: Parser[String] = digit ~ rep(digit) ^^ {
    case first ~ rest =>
      (first :: rest).mkString("")
  }

  private val stringLit: Parser[String] = new Parser[String] {
    def apply(in: Input): ParseResult[String] = {
      if (in.atEnd) {
        Failure("end of input", in)
      } else {
        val source = in.source
        val offset = in.offset

        val quoteRes = if (source.length - offset < 2) {
          // need at least 2 characters left to open and close the string
          Left(Failure("string literal expected", in))
        } else if (in.first == '\'') {
          Right('\'')
        } else {
          // need to open the string
          Left(Failure("' expected", in))
        }

        quoteRes match {
          case Left(f) =>
            // return the failure
            f
          case Right(quote) =>
            // find the end
            findEnd(in.rest, quote, escape = false) match {
              case Some(out) => Success(source.subSequence(offset + 1, out.offset - 1).toString, out)
              case None => Error("unclosed string literal", in)
            }
        }
      }
    }

    @tailrec
    private def findEnd(in: Input,
                        quote: Char,
                        escape: Boolean): Option[Input] = {
      if (in.atEnd || in.first == '\n') {
        // at the end, unclosed string
        None
      } else if (!escape && in.first == quote) {
        // closing quote not escaped, closed string
        Some(in.rest)
      } else {
        // keep looking, setting escape if we encounter a \ that was not escaped
        findEnd(in.rest, quote, !escape && in.first == '\\')
      }
    }
  }

  override def token: Parser[Token] = (
    identifier ^^ processIdent
      | numericLit ^^ NumericLit
      | stringLit ^^ StringLit
      | EofCh ^^^ EOF
      | delim
      | failure("illegal character")
    )
}
