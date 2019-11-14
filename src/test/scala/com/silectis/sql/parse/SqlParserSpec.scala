package com.silectis.sql.parse

import com.silectis.sql._
import com.silectis.test.UnitSpec

/**
  * @author dkotsiko
  *         jlouns
  */
// scalastyle:off magic.number
class SqlParserSpec extends UnitSpec {

  val parser = new SqlParser

  it should "parse a basic select statement" in {
    val parsed = parser.parse("select test, test2 from test_table")
    parsed shouldBe Query(
      Seq(
        ColumnReference("test"),
        ColumnReference("test2")
      ),
      Some(TableReference(None, "test_table"))
    )
  }

  it should "parse a select statement without a from clause" in {
    val parsed = parser.parse("select 1")
    parsed shouldBe Query(Seq(IntegerLiteral(1)))
  }

  it should "be case insensitive with respect to key words and identifiers" in {
    val parsed = parser.parse("Select test, TEst2 FRom TEST_table")
    parsed shouldBe Query(
      Seq(
        ColumnReference("test"),
        ColumnReference("test2")
      ),
      Some(TableReference(None, "test_table"))
    )
  }

  it should "fail on identifiers that contain special characters other than \"_\"" in {
    a[SqlException] should be thrownBy parser.parse("select asfs*~ from table")
    a[SqlException] should be thrownBy parser.parse("select *afdsfa from table")
  }

  it should "parse select statements with subqueries" in {
    val parsed = parser.parse("select test3 from (select test, test2 from test_table) as a")
    parsed shouldBe Query(
      Seq(ColumnReference("test3")),
      Some(Subquery(
        Query(
          Seq(
            ColumnReference("test"),
            ColumnReference("test2")
          ),
          Some(TableReference(None, "test_table"))
        ),
        "a"
      ))
    )
  }

  it should "parse select statements with aliases" in {
    val parsed1 = parser.parse("select test1 as t1, test2 as t2 from test_table")
    parsed1 shouldBe Query(
      Seq(
        ColumnAlias(ColumnReference("test1"), "t1"),
        ColumnAlias(ColumnReference("test2"), "t2")
      ),
      Some(TableReference(None, "test_table"))
    )

    val parsed2 = parser.parse("select test1 a1, test2 a2 from test_table as t")
    parsed2 shouldBe Query(
      Seq(
        ColumnAlias(ColumnReference("test1"), "a1"),
        ColumnAlias(ColumnReference("test2"), "a2")
      ),
      Some(TableAlias(TableReference(None, "test_table"), "t"))
    )
  }

  it should "parse literals" in {
    val parsed = parser.parse("select 'one', 2, 3.5, -1, -2.3, true, false, null as test")
    parsed shouldBe Query(
      Seq(
        StringLiteral("one"),
        IntegerLiteral(2),
        DecimalLiteral(BigDecimal("3.5")),
        IntegerLiteral(-1),
        DecimalLiteral(BigDecimal("-2.3")),
        BooleanLiteral(true),
        BooleanLiteral(false),
        ColumnAlias(NullLiteral, "test")
      )
    )
  }

  it should "parse string literals with with escaped quotes" in {
    val parsed = parser.parse("""select '\'' from test_table""")
    parsed shouldBe Query(
      Seq(StringLiteral("\\'")),
      Some(TableReference(None, "test_table"))
    )
  }

  it should "parse a SQL function" in {
    val parsed = parser.parse("select upper(test) from test_table")
    parsed shouldBe Query(
      Seq(SqlFunction("upper", Seq(ColumnReference("test")))),
      Some(TableReference(None, "test_table"))
    )
  }

  it should "parse a SQL function with an alias" in {
    val parsed = parser.parse("select sum(test) as sum_test from test_table")
    parsed shouldBe Query(
      Seq(ColumnAlias(SqlFunction("sum", Seq(ColumnReference("test"))), "sum_test")),
      Some(TableReference(None, "test_table"))
    )
  }
}
