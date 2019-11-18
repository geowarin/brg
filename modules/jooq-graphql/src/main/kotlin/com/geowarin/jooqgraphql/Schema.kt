package com.geowarin.jooqgraphql

import graphql.GraphQL
import graphql.Scalars
import graphql.schema.*
import graphql.schema.GraphQLList.list
import graphql.schema.GraphQLTypeReference.*
import org.jooq.*
import org.jooq.impl.SQLDataType

class Fk(
  wrapped: ForeignKey<out Record, out Record>,
  reversed: Boolean = false
) {
  val localTable: Table<*> = if (reversed) wrapped.key.table else wrapped.table
  val localFields: List<Field<*>> = if (reversed) wrapped.key.fields else wrapped.fields

  val foreignTable: Table<*> = if (reversed) wrapped.table else wrapped.key.table
  val foreignFields: List<Field<*>> = if (reversed) wrapped.fields else wrapped.key.fields
}

data class TableGraphNode(
  val table: Table<*>,
  val foreignKeys: List<Fk>,
  val reverseForeignKeys: List<Fk>
)

fun buildGraphQL(tableDataFetcher: TableDataFetcher, vararg tables: Table<*>): GraphQL {
  val queryType = GraphQLObjectType.newObject().name("QueryType")

  val tableGraphNodes = tables.map { table ->
    TableGraphNode(
      table = table,
      foreignKeys = table.references.map { Fk(it) },
      reverseForeignKeys = tables
        .mapNotNull { otherTable -> otherTable.references.find { it.key.table == table } }
        .map { Fk(it, reversed = true) }
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
    val refTable = ref.foreignTable
    typeBuilder.field { f ->
      f.type(list(typeRef(refTable.name)))
        .name(refTable.name + "s")
        .description(refTable.commentOrNull())
        .dataFetcher(fieldDataFetcher)
    }
  }
  for (ref in tableGraphNode.reverseForeignKeys) {
    val refTable = ref.foreignTable
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