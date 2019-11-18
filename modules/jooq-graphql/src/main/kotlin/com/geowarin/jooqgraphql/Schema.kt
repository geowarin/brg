package com.geowarin.jooqgraphql

import graphql.GraphQL
import graphql.Scalars
import graphql.schema.*
import graphql.schema.GraphQLList.list
import graphql.schema.GraphQLTypeReference.*
import org.jooq.*
import org.jooq.impl.SQLDataType


data class TableGraphNode(
  val table: Table<*>,
  val foreignKeys: List<ForeignKey<*, *>>,
  val reverseForeignKeys: List<ForeignKey<out Record, out Record>>
)

fun buildGraphQL(tableDataFetcher: TableDataFetcher, vararg tables: Table<*>): GraphQL {
  val queryType = GraphQLObjectType.newObject().name("QueryType")

  val tableGraphNodes = tables.map { table ->
    TableGraphNode(
      table = table,
      foreignKeys = table.references,
      reverseForeignKeys = tables.mapNotNull { otherTable -> otherTable.references.find { it.key.table == table } }
    )
  }
  tableGraphNodes.forEach { queryType.field(queryFromTable(tableDataFetcher, it)) }

  val schema = GraphQLSchema.newSchema().query(queryType).build()
  return GraphQL.newGraphQL(schema).build()
}

fun queryFromTable(tableDataFetcher: TableDataFetcher, tableGraphNode: TableGraphNode): GraphQLFieldDefinition.Builder {
  val table = tableGraphNode.table
  return GraphQLFieldDefinition.newFieldDefinition()
    .name(table.name)
    .type(list(graphQlTypeFromTable(tableGraphNode)))
    .description(table.commentOrNull())
    .dataFetcher { e -> tableDataFetcher(tableGraphNode, e) }
}

val fieldDataFetcher: (DataFetchingEnvironment) -> Any = {
  val source: Record = it.getSource()
  source.get(it.field.name)
}

private fun graphQlTypeFromTable(tableGraphNode: TableGraphNode): GraphQLObjectType {
  val table = tableGraphNode.table
  val typeBuilder = GraphQLObjectType
    .newObject()
    .name(table.name)

  for (field in table.fields()) {
    typeBuilder.field { f ->
      f.type(getType(field.dataType))
        .name(field.name)
        .description(field.commentOrNull())
        .dataFetcher(fieldDataFetcher)
    }
  }
  for (ref in tableGraphNode.foreignKeys) {
    val refTable = ref.key.table
    typeBuilder.field { f ->
      f.type(list(typeRef(refTable.name)))
        .name(refTable.name + "s")
        .description(refTable.commentOrNull())
        .dataFetcher(fieldDataFetcher)
    }
  }
  for (ref in tableGraphNode.reverseForeignKeys) {
    val refTable = ref.table
    typeBuilder.field { f ->
      f.type(list(typeRef(refTable.name)))
        .name(refTable.name + "s")
        .description(refTable.commentOrNull())
        .dataFetcher(fieldDataFetcher)
    }
  }

  return typeBuilder.build()
}

fun Field<*>.commentOrNull() = if (this.comment.isNotBlank()) this.comment else null
fun Table<*>.commentOrNull() = if (this.comment.isNotBlank()) this.comment else null

private fun getType(type: DataType<out Any>): GraphQLOutputType {
  return when (type) {
    SQLDataType.UUID -> Scalars.GraphQLID
    SQLDataType.VARCHAR -> Scalars.GraphQLString
    SQLDataType.BIGINT -> Scalars.GraphQLLong
    else -> Scalars.GraphQLString
  }
}