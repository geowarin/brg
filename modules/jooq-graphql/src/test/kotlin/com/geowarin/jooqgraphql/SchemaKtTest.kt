package com.geowarin.jooqgraphql

import com.geowarin.jooqgraphql.SqlAssert.Companion.assertThatSql
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.StringAssert
import org.intellij.lang.annotations.Language
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

class PersonTable : TableImpl<Record>(DSL.name("person")) {
  init {
    createField(DSL.name("first_name"), SQLDataType.VARCHAR)
  }
}

internal class SchemaKtTest {
  private val settings: Settings = Settings()
    .withRenderSchema(false)
    .withRenderQuotedNames(RenderQuotedNames.NEVER)

  private val jooq: DSLContext = DSL.using(SQLDialect.POSTGRES, settings)
  private val table = PersonTable()

  @Test
  fun test() {
    val query = getSqlQuery(
      """{
      person {
        first_name
      }
    }"""
    )
    assertThatSql(query).isEqualTo(
      """
      select person.first_name from person
      """
    )
  }

  private fun getSqlQuery(graphQlQuery: String): String {
    var query = ""
    val tableDataFetcher: TableDataFetcher = { table, e ->
      query = DataFetchers.DEFAULT_QUERY_GENERATOR(jooq, table, e).query.toString()
      emptyList()
    }
    val graphQL = makeGraphQL(tableDataFetcher, table)

    val executionInput = ExecutionInput.newExecutionInput()
      .query(graphQlQuery)
      .build()
    val result = graphQL.execute(executionInput)
    check(result.errors.isEmpty()) { result.errors.joinToString { it.message } }
    return query
  }
}

fun makeGraphQL(tableDataFetcher: TableDataFetcher, table: Table<*>): GraphQL = GraphQL.newGraphQL(
  GraphQLSchema.newSchema()
    .query(
      GraphQLObjectType.newObject()
        .name("QueryType")
        .field(queryFromTable(tableDataFetcher, table))
    ).build()
).build()

class SqlAssert(value: String) : AbstractAssert<StringAssert, String>(value, SqlAssert::class.java) {
  fun isEqualTo( expected: String): SqlAssert {
    Assertions.assertThat(actual.trim()).isEqualToIgnoringWhitespace(expected)
    return this
  }

  companion object {
    fun assertThatSql(value: String): SqlAssert {
      return SqlAssert(value)
    }
  }
}
