package com.geowarin.jooqgraphql

import com.geowarin.jooq.fkOn
import com.geowarin.jooq.table
import com.geowarin.jooqgraphql.utils.TestGraphQlSchema
import org.jooq.impl.SQLDataType

val personTable = table("person") {
  pk("id", SQLDataType.UUID)
  field("first_name", SQLDataType.VARCHAR)
  field("last_name", SQLDataType.VARCHAR)
  field("about", SQLDataType.CLOB)
  field("createdAt", SQLDataType.TIMESTAMP)
}


val postTable = table("post") {
  val id = pk("id", SQLDataType.UUID)
  val person_id = field("person_id", SQLDataType.UUID) fkOn personTable
  field("headline", SQLDataType.CLOB)
  field("body", SQLDataType.CLOB)
  field("topic", SQLDataType.VARCHAR)
  field("createdAt", SQLDataType.TIMESTAMP)
}

val postSchema = TestGraphQlSchema(personTable, postTable)