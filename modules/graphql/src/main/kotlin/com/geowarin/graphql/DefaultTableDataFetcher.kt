package com.geowarin.graphql

import graphql.schema.DataFetchingEnvironment
import org.jooq.Record
import org.jooq.Table

object DataFetchers {
  val NULL: TableDataFetcher = object : TableDataFetcher {
    override fun fetch(table: Table<*>, e: DataFetchingEnvironment): Iterable<Record> {
      return emptyList()
    }
  }
}
