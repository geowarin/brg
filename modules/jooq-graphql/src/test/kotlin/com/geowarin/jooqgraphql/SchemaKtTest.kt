package com.geowarin.jooqgraphql

import com.geowarin.jooq.table
import com.geowarin.jooqgraphql.SqlAssert.Companion.assertThatSql
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.StringAssert
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test

internal class SchemaKtTest {
  private val settings: Settings = Settings()
    .withRenderSchema(false)
    .withRenderQuotedNames(RenderQuotedNames.NEVER)

  private val jooq: DSLContext = DSL.using(SQLDialect.POSTGRES, settings)

  @Test
  fun `select one field`() {
    val personTable = table("person") {
      field("first_name", SQLDataType.VARCHAR)
    }

    val query = getSqlQuery(
      """{
        person {
          first_name
        }
      }""",
      personTable
    )
    assertThatSql(query).isEqualTo(
      """
      select person.first_name from person
      """
    )
  }

  @Test
  fun `fk direct way`() {
    val personTable = table("person") {
      pk("id", SQLDataType.UUID)
      field("first_name", SQLDataType.VARCHAR)
      field("last_name", SQLDataType.VARCHAR)
      field("about", SQLDataType.CLOB)
      field("createdAt", SQLDataType.TIMESTAMP)
    }

    val postTable = table("post") {
      pk("id", SQLDataType.UUID)
      field("id", SQLDataType.UUID) fkOn personTable
      field("headline", SQLDataType.CLOB)
      field("body", SQLDataType.CLOB)
      field("topic", SQLDataType.VARCHAR)
      field("createdAt", SQLDataType.TIMESTAMP)
    }

    val query = getSqlQuery(
      """{
        post {
          headline
          
          persons {
            first_name
          }
        }
      }""",
      personTable, postTable
    )
    assertThatSql(query).isEqualTo(
      """
      select 
        post.headline, 
        person.first_name
      from post
        join person
          on post.id = person.id
      """
    )
  }

  private fun getSqlQuery(graphQlQuery: String, vararg tables: Table<*>): String {
    var query = ""
    val tableDataFetcher: TableDataFetcher = { table, e ->
      query = DataFetchers.DEFAULT_QUERY_GENERATOR(jooq, table, e).query.toString()
      emptyList()
    }
    val graphQL = makeGraphQL(tableDataFetcher, *tables)

    val executionInput = ExecutionInput.newExecutionInput()
      .query(graphQlQuery)
      .build()
    val result = graphQL.execute(executionInput)
    check(result.errors.isEmpty()) { result.errors.joinToString { it.message } }
    return query
  }
}

fun makeGraphQL(tableDataFetcher: TableDataFetcher, vararg tables: Table<*>): GraphQL {
  val queryType = GraphQLObjectType.newObject().name("QueryType")
  tables.forEach { queryType.field(queryFromTable(tableDataFetcher, it)) }

  return GraphQL.newGraphQL(
    GraphQLSchema.newSchema()
      .query(
        queryType
      ).build()
  ).build()
}

class SqlAssert(value: String) : AbstractAssert<StringAssert, String>(value, SqlAssert::class.java) {
  fun isEqualTo(expected: String): SqlAssert {
    Assertions.assertThat(actual.trim()).isEqualToIgnoringWhitespace(expected)
    return this
  }

  companion object {
    fun assertThatSql(value: String): SqlAssert {
      return SqlAssert(value)
    }
  }
}
