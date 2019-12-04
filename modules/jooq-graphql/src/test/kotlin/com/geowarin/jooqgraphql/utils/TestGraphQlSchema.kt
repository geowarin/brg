package com.geowarin.jooqgraphql.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.geowarin.jooqgraphql.buildGraphQL
import com.geowarin.jooqgraphql.buildNonExecutableGraphQL
import graphql.ExecutionInput.newExecutionInput
import graphql.introspection.IntrospectionQuery.INTROSPECTION_QUERY
import graphql.introspection.IntrospectionResultToSchema
import graphql.schema.idl.SchemaPrinter
import org.jooq.DDLFlag
import org.jooq.DSLContext
import org.jooq.Table

class TestGraphQlSchema(
  private vararg val tables: Table<*>
) {

  internal fun getSqlQuery(graphQlQuery: String): String {
    var query = ""
    val graphQL = buildGraphQL(
      queryExecutionStrategy = { _, q ->
        query = q.sql
        emptyList<Any>()
      },
      tables = *tables
    )

    val executionInput = newExecutionInput()
      .query(graphQlQuery)
      .build()
    val result = graphQL.execute(executionInput)
    check(result.errors.isEmpty()) { result.errors.joinToString { it.message } }
    return query
  }

  internal fun executeGraphqlQuery(jooq: DSLContext, graphQlQuery: String): String {
    val graphQL = buildGraphQL(dsl = jooq, tables = *tables)

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
    val graphQL = buildNonExecutableGraphQL(tables = *tables)
    val schemaResult = graphQL.execute(newExecutionInput().query(INTROSPECTION_QUERY).build())
    val schemaDocument = IntrospectionResultToSchema().createSchemaDefinition(schemaResult)
    val schemaString = SchemaPrinter().print(schemaDocument)
    println(schemaString)
  }
}
