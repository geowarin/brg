package com.geowarin.graphql

import com.geowarin.jooqgraphql.TableDataFetcher
import com.geowarin.jooqgraphql.queryFromTable
import com.geowarin.model.brg_security.Tables
import graphql.GraphQL
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema

class GraphQLFactory(
  private val tableDataFetcher: TableDataFetcher
) {

  fun makeGraphQL(): GraphQL = GraphQL.newGraphQL(buildSchema()).build()

  private fun buildSchema(): GraphQLSchema = GraphQLSchema.newSchema()
    .query(
      GraphQLObjectType.newObject()
        .name("QueryType")
        .field(queryFromTable(tableDataFetcher, Tables.BRG_USER))
    ).build()

}