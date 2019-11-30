package com.geowarin.jooqgraphql

import graphql.schema.DataFetchingEnvironment
import org.jooq.*

typealias TableDataFetcher = (TableGraphNode, DataFetchingEnvironment) -> Any

object DataFetchers {
  val NULL: TableDataFetcher = { _, _ -> emptyList<Any>() }

  val DEFAULT: (jooq: DSLContext) -> TableDataFetcher = jooqTableDataFetcher
}

