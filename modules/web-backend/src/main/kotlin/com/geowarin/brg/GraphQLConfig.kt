package com.geowarin.brg

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
    TODO()
//    val tableDataFetcher = DataFetchers.DEFAULT(jooq)
//    return GraphQLFactory(tableDataFetcher).makeGraphQL()
  }
}
