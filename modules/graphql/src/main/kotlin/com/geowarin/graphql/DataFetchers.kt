package com.geowarin.graphql

import graphql.schema.DataFetchingEnvironment
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectFinalStep
import org.jooq.Table

typealias TableDataFetcher = (Table<*>, DataFetchingEnvironment) -> Iterable<Record>
typealias QueryGenerator = (DSLContext, Table<*>, DataFetchingEnvironment) -> SelectFinalStep<Record>

object DataFetchers {
  val NULL: TableDataFetcher = { _, _ -> emptyList() }

  val DEFAULT: (jooq: DSLContext) -> TableDataFetcher = { jooq ->
    { table, e ->
      val query = DEFAULT_QUERY_GENERATOR(jooq, table, e)
      query.fetch()
    }
  }

  val DEFAULT_QUERY_GENERATOR: QueryGenerator = { jooq, table, e ->
    val sqlFields = e.selectionSet.fields.map { table.field(it.name) }
    jooq.select(sqlFields).from(table)
  }
}
