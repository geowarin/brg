package com.geowarin.graphql

import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import org.jooq.Record
import org.jooq.Table

interface TableDataFetcher {
  fun fetch(table: Table<*>, e: DataFetchingEnvironment): Iterable<Record>
}

interface GraphQLFactory {
  fun makeGraphQL(): GraphQL
}
