/*
 * Copyright (C) Silectis, Inc - All Rights Reserved.
 *
 *  All information contained herein is, and remains the property of Silectis, Inc. The intellectual
 *  and technical concepts contained herein are proprietary to Silectis, Inc and are protected by
 *  trade secret or copyright law. Dissemination of this information or reproduction of this
 *  material is strictly forbidden unless prior written permission is obtained from Silectis, Inc.
 */

package com.silectis.sql.parse

import com.silectis.test.UnitSpec
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

/**
 * @author jlouns
 */
class SqlLexicalSpec extends UnitSpec
  with LazyLogging {

  class LexicalHelper extends SqlLexical {
    def getTokens(input: String): Seq[Token] = getTokensInternal(new Scanner(input), Seq())

    @tailrec
    private def getTokensInternal(scanner: Scanner, tokens: Seq[Token]): Seq[Token] = {
      if (scanner.atEnd) {
        tokens
      } else {
        getTokensInternal(scanner.rest, tokens :+ scanner.first)
      }
    }
  }

  private val lexical = new LexicalHelper
  import lexical._

  private def hasParseError(input: String): Boolean = {
    logger.debug(s"input: $input")
    val tokens = lexical.getTokens(input)
    logger.debug(s"tokens: $tokens")
    tokens.exists(_.isInstanceOf[ErrorToken])
  }

  it should "successfully tokenize strings" in {
    val content = "test string"

    val tokens = lexical.getTokens("'" + content + "'")

    logger.debug(tokens.toString)

    tokens should contain only StringLit(content)
  }

  it should "successfully tokenize an empty string" in {
    val content = ""

    val tokens = lexical.getTokens("'" + content + "'")

    logger.debug(tokens.toString)

    tokens should contain only StringLit(content)
  }

  it should "successfully tokenize strings alongside other content" in {
    val content = "test string"

    val tokens = lexical.getTokens("'" + content + "' and some others")

    logger.debug(tokens.toString)

    tokens should have size 4

    val string = tokens.head
    string shouldBe a[StringLit]
    string.chars shouldBe content
  }

  it should "allow quotes to be escaped in a string" in {
    val content = """test \' string"""

    val tokens = lexical.getTokens("'" + content + "'")

    logger.debug(tokens.toString)

    tokens should contain only StringLit(content)
  }

  it should "not escape a quote if the escape is escaped" in {
    val content = """test string\\"""

    val tokens = lexical.getTokens("'" + content + "' and more")

    logger.debug(tokens.toString)

    tokens should have size 3

    val string = tokens.head
    string shouldBe a[StringLit]
    string.chars shouldBe content
  }

  it should "pass through java escapes in string literals" in {
    val content = """test \t string"""

    val tokens = lexical.getTokens("'" + content + "'")

    logger.debug(tokens.toString)

    tokens should contain only StringLit(content)
  }

  it should "return an error for an unclosed string" in {
    val content = "test string"

    hasParseError("'" + content) shouldBe true
  }

  it should "return an error for a string that spans multiple lines" in {
    val content = "test\nstring"

    hasParseError("'" + content + "'") shouldBe true
  }

  it should "fail to tokenize strings with illegal characters" in {
    hasParseError("test ?") shouldBe true
  }

  it should "tokenize complicated strings" in {
    val content = """
                    |select col1, 2.5, 'test'
                    |from my_table
                    |""".stripMargin

    val tokens = lexical.getTokens(content)

    logger.debug(tokens.toString)

    tokens shouldBe Seq(
      Keyword("select"),
      Identifier("col1"), Keyword(","),
      NumericLit("2"), Keyword("."), NumericLit("5"), Keyword(","),
      StringLit("test"),
      Keyword("from"),
      Identifier("my_table")
    )
  }
}
