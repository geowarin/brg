package com.geowarin.brg.graphql

import graphql.schema.DataFetchingEnvironment
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Table
import org.springframework.stereotype.Component

object DataFetchers {
  val NULL: TableDataFetcher = object : TableDataFetcher {
    override fun fetch(table: Table<*>, e: DataFetchingEnvironment): Iterable<Record> {
      return emptyList()
    }
  }
}

@Component
class DefaultTableDataFetcher(
  val jooq: DSLContext
): TableDataFetcher {
  override fun fetch(table: Table<*>, e: DataFetchingEnvironment): Iterable<Record> {
    return jooq.select().from(table).fetch()
  }
}