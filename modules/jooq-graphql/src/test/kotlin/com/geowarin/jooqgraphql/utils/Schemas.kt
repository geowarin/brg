package com.geowarin.jooqgraphql.utils

import com.geowarin.jooq.fkOn
import com.geowarin.jooq.table
import org.jooq.impl.SQLDataType

val personTable = table("person") {
  pk("id", SQLDataType.UUID)
  field("first_name", SQLDataType.VARCHAR)
  field("last_name", SQLDataType.VARCHAR)
  field("about", SQLDataType.CLOB)
  field("createdAt", SQLDataType.TIMESTAMP)
}

val postTable = table("post") {
  pk("id", SQLDataType.UUID)
  field("person_id", SQLDataType.UUID) fkOn personTable
  field("headline", SQLDataType.CLOB)
  field("body", SQLDataType.CLOB)
  field("topic", SQLDataType.VARCHAR)
  field("createdAt", SQLDataType.TIMESTAMP)
}

val postSchema = TestGraphQlSchema(
  personTable,
  postTable
)
