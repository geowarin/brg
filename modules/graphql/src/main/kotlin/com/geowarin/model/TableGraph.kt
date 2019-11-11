package com.geowarin.model

import org.jooq.Table

class Node(
  val table: Table<*>
)

class TableGraph {

  constructor() {
    val allTables = DefaultCatalog.DEFAULT_CATALOG.schemas.flatMap { it.tables }
    allTables.forEach { table ->

      table.references.forEach {
      }
    }
  }
}