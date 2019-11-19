package com.geowarin.jooqgraphql.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.geowarin.jooqgraphql.DataFetchers
import com.geowarin.jooqgraphql.TableDataFetcher
import com.geowarin.jooqgraphql.buildGraphQL
import graphql.ExecutionInput.newExecutionInput
import graphql.introspection.IntrospectionQuery.INTROSPECTION_QUERY
import graphql.introspection.IntrospectionResultToSchema
import graphql.schema.idl.SchemaPrinter
import org.jooq.DDLFlag
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL

data class DbCredentials(
  val jdbcUrl: String,
  val username: String,
  val password: String
)

class TestGraphQlSchema(
  private vararg val tables: Table<*>
) {
  private val settings: Settings = Settings()
    .withRenderSchema(false)
    .withRenderQuotedNames(RenderQuotedNames.NEVER)

  private val jooq: DSLContext = DSL.using(SQLDialect.POSTGRES, settings)

  internal fun getSqlQuery(graphQlQuery: String): String {
    var query = ""
    val tableDataFetcher: TableDataFetcher = { table, e ->
      query = DataFetchers.DEFAULT_QUERY_GENERATOR(jooq, table, e).query.toString()
      emptyList()
    }
    val graphQL = buildGraphQL(tableDataFetcher, *tables)

    val executionInput = newExecutionInput()
      .query(graphQlQuery)
      .build()
    val result = graphQL.execute(executionInput)
    check(result.errors.isEmpty()) { result.errors.joinToString { it.message } }
    return query
  }

  internal fun executeGraphqlQuery(jooq: DSLContext, graphQlQuery: String): String {
    val graphQL = buildGraphQL(DataFetchers.DEFAULT(jooq), *tables)

    val executionInput = newExecutionInput()
      .query(graphQlQuery)
      .build()
    val result = graphQL.execute(executionInput)
    check(result.errors.isEmpty()) { result.errors.joinToString { it.message } }
    return ObjectMapper().writeValueAsString(result.toSpecification())
  }

  fun executeDDL(jooq: DSLContext) {
    val ddl = jooq.ddl(tables, DDLFlag.TABLE, DDLFlag.FOREIGN_KEY, DDLFlag.PRIMARY_KEY, DDLFlag.UNIQUE)
    for (query in ddl) {
      println(query)
    }
    ddl.executeBatch()
  }

  fun print() {
    val graphQL = buildGraphQL(DataFetchers.NULL, *tables)
    val schemaResult = graphQL.execute(newExecutionInput().query(INTROSPECTION_QUERY).build())
    val schemaDocument = IntrospectionResultToSchema().createSchemaDefinition(schemaResult)
    val schemaString = SchemaPrinter().print(schemaDocument)
    println(schemaString)
  }
}