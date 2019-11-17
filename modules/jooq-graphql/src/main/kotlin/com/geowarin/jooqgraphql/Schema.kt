package com.geowarin.jooqgraphql

import graphql.Scalars
import graphql.schema.*
import graphql.schema.GraphQLTypeReference.*
import org.jooq.DataType
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.SQLDataType


fun <T : Record> queryFromTable(tableDataFetcher: TableDataFetcher, table: Table<T>): GraphQLFieldDefinition.Builder {
  return GraphQLFieldDefinition.newFieldDefinition()
    .name(table.name)
    .type(GraphQLList.list(graphQlTypeFromTable(table)))
    .description(table.comment)
    .dataFetcher { e -> tableDataFetcher(table, e) }
}

private fun <T : Record> graphQlTypeFromTable(table: Table<T>): GraphQLObjectType {
  val typeBuilder = GraphQLObjectType
    .newObject()
    .name(table.name)

  for (field in table.fields()) {
    typeBuilder.field { f ->
      f.type(getType(field.dataType))
        .name(field.name)
        .description(field.comment)
        .dataFetcher {
          val source: T = it.getSource()
          source.get(it.field.name)
        }
    }
  }
//  val referencesFields = table.references.map { it.fields.first() }
  for (ref in table.references) {
    val field = ref.fields.first()
    typeBuilder.field { f ->
      f.type(typeRef(ref.key.table.name))
        .name(ref.key.table.name + "s")
//        .description(field.comment)
        .dataFetcher {
          val source: T = it.getSource()
          source.get(it.field.name)
        }
    }
  }


  return typeBuilder.build()
}

private fun getType(type: DataType<out Any>): GraphQLOutputType {
  return when (type) {
    SQLDataType.UUID -> Scalars.GraphQLID
    SQLDataType.VARCHAR -> Scalars.GraphQLString
    SQLDataType.BIGINT -> Scalars.GraphQLLong
    else -> Scalars.GraphQLString
  }
}