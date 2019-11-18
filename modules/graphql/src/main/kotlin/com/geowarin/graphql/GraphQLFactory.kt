package com.geowarin.graphql

import com.geowarin.jooqgraphql.TableDataFetcher
import com.geowarin.jooqgraphql.buildGraphQL
import com.geowarin.model.brg_security.Tables
import graphql.GraphQL

class GraphQLFactory(
  private val tableDataFetcher: TableDataFetcher
) {
  fun makeGraphQL(): GraphQL = buildGraphQL(tableDataFetcher, Tables.BRG_USER)
}