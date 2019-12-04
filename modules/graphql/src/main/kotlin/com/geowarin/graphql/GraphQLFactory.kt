package com.geowarin.graphql

import com.geowarin.jooqgraphql.buildGraphQL
import com.geowarin.jooqgraphql.buildNonExecutableGraphQL
import com.geowarin.model.brg_security.Tables
import graphql.GraphQL
import org.jooq.DSLContext

class GraphQLFactory(
  private val dsl: DSLContext? = null
) {
  fun makeGraphQL(): GraphQL {
    if (dsl == null) {
      return buildNonExecutableGraphQL(tables = *arrayOf(Tables.BRG_USER))
    }
    return buildGraphQL(dsl = dsl, tables = *arrayOf(Tables.BRG_USER))
  }
}
