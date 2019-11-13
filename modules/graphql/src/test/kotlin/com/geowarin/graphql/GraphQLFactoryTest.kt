package com.geowarin.graphql

import com.geowarin.graphql.SqlAssert.Companion.assertThatSql
import com.geowarin.jooqgraphql.DataFetchers.DEFAULT_QUERY_GENERATOR
import com.geowarin.jooqgraphql.TableDataFetcher
import graphql.ExecutionInput
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.StringAssert
import org.intellij.lang.annotations.Language
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test

internal class GraphQLFactoryTest {
  private val settings: Settings = Settings()
//    .withRenderSchema(false)
    .withRenderQuotedNames(RenderQuotedNames.NEVER)
  private val jooq: DSLContext = DSL.using(SQLDialect.POSTGRES, settings)

  @Test
  fun `should only select query fields`() {
    val query = getSqlQuery(
      """{
      brg_user {
        first_name
      }
    }"""
    )
    assertThatSql(query).isEqualTo(
      """
      select brg_security.brg_user.first_name from brg_security.brg_user
      """
    )
  }

  private fun getSqlQuery(@Language("graphql") graphQlQuery: String): String {
    var query = ""
    val tableDataFetcher: TableDataFetcher = { table, e ->
      query = DEFAULT_QUERY_GENERATOR(jooq, table, e).query.toString()
      emptyList()
    }
    val graphQL = GraphQLFactory(tableDataFetcher).makeGraphQL()

    val executionInput = ExecutionInput.newExecutionInput()
      .query(graphQlQuery)
      //      .operationName(invocationData.getOperationName())
      //      .variables(invocationData.getVariables())
      .build()
    graphQL.execute(executionInput)
    return query
  }
}


class SqlAssert(value: String) : AbstractAssert<StringAssert, String>(value, SqlAssert::class.java) {
  fun isEqualTo(@Language("sql") expected: String): SqlAssert {
    assertThat(actual.trim()).isEqualToIgnoringWhitespace(expected)
    return this
  }

  companion object {
    fun assertThatSql(value: String): SqlAssert {
      return SqlAssert(value)
    }
  }
}
