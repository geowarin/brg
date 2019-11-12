package com.geowarin.brg

import com.geowarin.graphql.DataFetchers
import com.geowarin.graphql.GraphQLFactory
import graphql.GraphQL
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfig(
  val jooq: DSLContext
) {

  @Bean
  fun graphQL(): GraphQL {
    val tableDataFetcher = DataFetchers.DEFAULT(jooq)
    return GraphQLFactory(tableDataFetcher).makeGraphQL()
  }
}
