package com.geowarin.brg

import com.geowarin.graphql.DefaultGraphQLFactory
import com.geowarin.graphql.TableDataFetcher
import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Table
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
class GraphQLConfig(
  val dataFetcherFactory: TableDataFetcher
) {

  @Bean
  fun graphQL(): GraphQL = DefaultGraphQLFactory(dataFetcherFactory).makeGraphQL()
}

@Component
class DefaultTableDataFetcher(
  val jooq: DSLContext
) : TableDataFetcher {
  override fun fetch(table: Table<*>, e: DataFetchingEnvironment): Iterable<Record> {
    return jooq.select().from(table).fetch()
  }
}