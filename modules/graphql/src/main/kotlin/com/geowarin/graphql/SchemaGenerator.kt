package com.geowarin.graphql

import com.geowarin.jooqgraphql.DataFetchers
import graphql.ExecutionInput
import graphql.introspection.IntrospectionQuery
import graphql.introspection.IntrospectionResultToSchema
import graphql.schema.idl.SchemaPrinter
import java.io.File

fun main(args: Array<String>) {
  val outputFile = args[0]

  val graphQL = GraphQLFactory(DataFetchers.NULL).makeGraphQL()
  val schemaResult = graphQL.execute(ExecutionInput.newExecutionInput().query(IntrospectionQuery.INTROSPECTION_QUERY).build())
  val schemaDocument = IntrospectionResultToSchema().createSchemaDefinition(schemaResult)
  File(outputFile).writeText(SchemaPrinter().print(schemaDocument))
}