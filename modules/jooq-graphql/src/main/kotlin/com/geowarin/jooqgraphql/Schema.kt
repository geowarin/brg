package com.geowarin.jooqgraphql

import graphql.GraphQL
import graphql.Scalars
import graphql.schema.*
import graphql.schema.GraphQLList.list
import graphql.schema.GraphQLTypeReference.typeRef
import org.jooq.*
import org.jooq.impl.SQLDataType

private fun findFk(tableGraphNode: TableGraphNode, fkGraphqlField: SelectedField): Fk {
  return tableGraphNode.foreignKeys.find { it.foreignTable.name == fkGraphqlField.name }
    ?: tableGraphNode.reverseForeignKeys.find { it.foreignTable.name == fkGraphqlField.name.removeSuffix("s") }
    ?: throw IllegalStateException("Could not find fk for field ${fkGraphqlField.name}")
}

data class Join(
  val foreignKey: Fk,
  val subGraphqlFields: MutableList<SelectedField>
) {
  val fkTable = foreignKey.foreignTable
  val sqlFields = subGraphqlFields.map { fkTable.field(it.name) }

  fun getJoinCondition(): Condition {
    val fkFields = foreignKey.foreignFields as List<Field<Any?>>
    val localFields = foreignKey.localFields as List<Any>

    val localField = localFields.first()
    val fkField = fkFields.first()
    return fkField.equal(localField)
  }

  fun generateJoin(query: SelectJoinStep<Record>): SelectJoinStep<Record> {
    return query.join(fkTable).on(getJoinCondition())
  }
}

val jooqTableDataFetcher: (DSLContext) -> (TableGraphNode, DataFetchingEnvironment) -> Any = { jooq ->
  { tableGraphNode, e ->
    val query = defaultQueryGenerator(jooq, tableGraphNode, e)
    val rootTable = tableGraphNode.table

    val queryResult = query.fetch()
    val resultGroupedByRootPK = queryResult.intoGroups(rootTable)
    resultGroupedByRootPK.toList()
  }
}

fun defaultQueryGenerator(
  jooq: DSLContext,
  tableGraphNode: TableGraphNode,
  e: DataFetchingEnvironment
): SelectJoinStep<Record> {
  val rootFields = e.selectionSet.getFields("*")
  val fkGraphqlFields = rootFields.filter { isFkField(it) }
  val joins = fkGraphqlFields.map { fkGraphqlField ->
    val foreignKey = findFk(tableGraphNode, fkGraphqlField)
    val subGraphqlFields = e.selectionSet.getFields("${fkGraphqlField.name}/*")

    Join(foreignKey = foreignKey, subGraphqlFields = subGraphqlFields)
  }

  val rootFields2 = e.selectionSet.getFields("*")
  val scalarFields = rootFields2.filter { !isFkField(it) }
  val rootTable = tableGraphNode.table
  val scalarSqlFields = scalarFields.map { rootTable.field(it.name) }
  val sqlFields = scalarSqlFields + joins.flatMap { it.sqlFields }
  //+ rootTable.primaryKey.fields

  var query = jooq.select(sqlFields).from(rootTable)
  joins.forEach { join ->
    query = join.generateJoin(query)
  }

  return query
}

private fun isFkField(it: SelectedField): Boolean {
  val type = it.fieldDefinition.type
  val isObjectList = type is GraphQLList && type.wrappedType is GraphQLObjectType
  return isObjectList || type is GraphQLObjectType
}

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
        .dataFetcher {

          println(it.field.name)
          when (val source = it.getSource<Any>()) {
              is Pair<*, *> -> { // Root
                (source.first as Record).get(it.field.name)
              }
              is Record -> { // reverse FK
                source.get(it.field.name)
              }
              is Result<*> -> { // direct FK
                source.first().get(it.field.name)
              }
              else -> {
                throw IllegalStateException("Oups")
              }
          }
        }
    }
  }
  for (ref in tableGraphNode.foreignKeys) {
    val refTable = ref.foreignTable
    typeBuilder.field { f ->
      f.type(typeRef(refTable.name))
        .name(refTable.name)
        .description(refTable.commentOrNull())
        .dataFetcher {
          val record: Pair<Record, Result<Record>> = it.getSource()
          record.second
        }
    }
  }
  for (ref in tableGraphNode.reverseForeignKeys) {
    val refTable = ref.foreignTable
    typeBuilder.field { f ->
      f.type(list(typeRef(refTable.name)))
        .name(refTable.name + "s")
        .description(refTable.commentOrNull())
        .dataFetcher {
          val record: Pair<Record, Result<Record>> = it.getSource()
          record.second
        }
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
