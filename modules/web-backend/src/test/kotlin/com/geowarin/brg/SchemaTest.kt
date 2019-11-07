package com.geowarin.brg

import com.geowarin.brg.graphql.DataFetchers
import com.geowarin.brg.graphql.GraphQLConfig
import com.geowarin.brg.graphql.GraphQLFactory
import graphql.ExecutionInput
import graphql.introspection.IntrospectionQuery
import graphql.introspection.IntrospectionResultToSchema
import graphql.schema.idl.SchemaPrinter
import org.junit.jupiter.api.Test

class SchemaTest {

  @Test
  fun `should get schema`() {
    val graphQLFactory: GraphQLFactory =
      GraphQLConfig(DataFetchers.NULL)
    val graphQL = graphQLFactory.makeGraphQL()
    val schemaResult = graphQL.execute(ExecutionInput.newExecutionInput().query(IntrospectionQuery.INTROSPECTION_QUERY).build())
    val schemaDocument = IntrospectionResultToSchema().createSchemaDefinition(schemaResult)
    println(SchemaPrinter().print(schemaDocument))
  }
}