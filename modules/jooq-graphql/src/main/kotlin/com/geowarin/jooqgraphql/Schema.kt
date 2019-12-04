package com.geowarin.jooqgraphql

import graphql.GraphQL
import graphql.Scalars
import graphql.schema.*
import graphql.schema.GraphQLList.list
import graphql.schema.GraphQLTypeReference.typeRef
import org.jooq.*
import org.jooq.impl.SQLDataType

typealias TableGraphNode = TableNode

val Dep.localFields: List<Field<*>>
    get() = this.fields

val Dep.foreignFields: List<Field<*>>
    get() = this.key.fields

val Dep.foreignTable: Table<*>
    get() = this.key.table

val Dep.localTable: Table<*>
    get() = this.table

data class Join(
  val foreignKey: Dep,
  val subGraphqlFields: MutableList<SelectedField>,
  val reverse: Boolean = false
) {
  val fkTable = if (reverse) foreignKey.localTable else foreignKey.foreignTable
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
    val rootTable = tableGraphNode.data

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

    val subGraphqlFields = e.selectionSet.getFields("${fkGraphqlField.name}/*")

    val directFk = tableGraphNode.dependencyFks.find { it.key.table.name == fkGraphqlField.name }
    val reverseFk = tableGraphNode.dependantFks.find { it.table.name == fkGraphqlField.name.removeSuffix("s") }

    if (directFk != null) {
      Join(foreignKey = directFk, subGraphqlFields = subGraphqlFields)
    } else if (reverseFk != null) {
      Join(foreignKey = reverseFk, subGraphqlFields = subGraphqlFields, reverse = true)
    } else {
      throw IllegalStateException("Could not find fk for field ${fkGraphqlField.name}")
    }
  }

  val rootFields2 = e.selectionSet.getFields("*")
  val scalarFields = rootFields2.filter { !isFkField(it) }
  val rootTable = tableGraphNode.data
  val scalarSqlFields = scalarFields.map { rootTable.field(it.name) }
  val sqlFields = scalarSqlFields + joins.flatMap { it.sqlFields }

  var query = jooq.select(sqlFields).from(rootTable)
  joins.forEach { join ->
    query = join.generateJoin(query)
  }

  return query
}

private fun isFkField(selectedField: SelectedField): Boolean {
  return !GraphQLTypeUtil.isLeaf(selectedField.fieldDefinition.type)
}

fun buildGraphQL(tableDataFetcher: TableDataFetcher, vararg tables: Table<*>): GraphQL {
  val queryType = GraphQLObjectType.newObject().name("QueryType")

  val tableDependencyGraph = TableDependencyGraph()
  tables.map { table ->
    tableDependencyGraph.addDependencies(table, *table.references.toTypedArray())
  }

  tables.forEach { table -> queryType.field(queryFromTable(tableDataFetcher, table, tableDependencyGraph)) }

  val schema = GraphQLSchema.newSchema().query(queryType).build()
  return GraphQL.newGraphQL(schema).build()
}

fun queryFromTable(
  tableDataFetcher: TableDataFetcher,
  table: Table<*>,
  tableDependencyGraph: TableDependencyGraph
): GraphQLFieldDefinition.Builder {
  val tableGraphNode = tableDependencyGraph.getNode(table)
  return GraphQLFieldDefinition.newFieldDefinition()
    .name(table.name)
    .type(list(graphQlTypeFromTable(tableGraphNode)))
    .description(table.commentOrNull())
    .dataFetcher { e -> tableDataFetcher(tableGraphNode, e) }
}

private fun graphQlTypeFromTable(tableGraphNode: TableGraphNode): GraphQLObjectType {
  val table = tableGraphNode.data
  val typeBuilder = GraphQLObjectType
    .newObject()
    .name(table.name)

  for (field in table.fields()) {
    typeBuilder.field { f ->
      f.type(getType(field.dataType))
        .name(field.name)
        .description(field.commentOrNull())
        .dataFetcher {

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
  for (refTable in tableGraphNode.dependencies) {
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
  for (refTable in tableGraphNode.dependants) {
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
