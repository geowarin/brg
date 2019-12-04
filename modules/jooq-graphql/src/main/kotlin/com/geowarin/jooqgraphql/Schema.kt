package com.geowarin.jooqgraphql

import graphql.GraphQL
import graphql.Scalars
import graphql.schema.*
import graphql.schema.GraphQLList.list
import graphql.schema.GraphQLTypeReference.typeRef
import org.jooq.*
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

typealias SqlQuery = SelectJoinStep<Record>
typealias QueryExecutionStrategy = (TableNode, SqlQuery) -> Any

private val defaultDsl: DSLContext = DSL.using(SQLDialect.POSTGRES, Settings()
  .withRenderSchema(false)
  .withRenderQuotedNames(RenderQuotedNames.NEVER)
)

fun buildNonExecutableGraphQL(vararg tables: Table<*>) = buildGraphQL(
  queryExecutionStrategy = { _, _ -> emptyList<Any>() },
  tables = *tables
)

fun buildGraphQL(dsl: DSLContext, vararg tables: Table<*>) = buildGraphQL(
  queryExecutionStrategy = { tableNode, sqlQuery -> executeQuery(dsl, tableNode, sqlQuery) },
  tables = *tables
)

internal fun buildGraphQL(
  queryExecutionStrategy: QueryExecutionStrategy,
  vararg tables: Table<*>
): GraphQL {

  val tableDependencyGraph = TableDependencyGraph()
  tables.map { table ->
    tableDependencyGraph.addDependencies(table, *table.references.toTypedArray())
  }

  val queryType = GraphQLObjectType.newObject().name("QueryType")
  tables.forEach { table ->
    val tableGraphNode = tableDependencyGraph.getNode(table)
    queryType.field(GraphQLFieldDefinition.newFieldDefinition()
      .name(table.name)
      .type(list(graphQlTypeFromTable(tableGraphNode)))
      .description(table.commentOrNull())
      .dataFetcher { e ->
        val query = generateQuery(tableGraphNode, e)
        queryExecutionStrategy(tableGraphNode, query)
      })
  }

  val schema = GraphQLSchema.newSchema().query(queryType).build()
  return GraphQL.newGraphQL(schema).build()
}

private fun generateQuery(
  tableGraphNode: TableNode,
  e: DataFetchingEnvironment
): SelectJoinStep<Record> {
  val rootFields = e.selectionSet.getFields("*")
  val fkGraphqlFields = rootFields.filter { isFkField(it) }
  val joins = fkGraphqlFields.map { fkGraphqlField ->

    val subGraphqlFields = e.selectionSet.getFields("${fkGraphqlField.name}/*")

    val directFk = tableGraphNode.dependencyFks.find { it.key.table.name == fkGraphqlField.name }
    val reverseFk = tableGraphNode.dependantFks.find { it.table.name == fkGraphqlField.name.removeSuffix("s") }

    when {
      directFk != null -> {
        Join(foreignKey = directFk, subGraphqlFields = subGraphqlFields)
      }
      reverseFk != null -> {
        Join(foreignKey = reverseFk, subGraphqlFields = subGraphqlFields, reverse = true)
      }
      else -> {
        throw IllegalStateException("Could not find fk for field ${fkGraphqlField.name}")
      }
    }
  }

  val rootFields2 = e.selectionSet.getFields("*")
  val scalarFields = rootFields2.filter { !isFkField(it) }
  val rootTable = tableGraphNode.data
  val scalarSqlFields = scalarFields.map { rootTable.field(it.name) }
  val sqlFields = scalarSqlFields + joins.flatMap { it.sqlFields }

  var query = defaultDsl.select(sqlFields).from(rootTable)
  joins.forEach { join ->
    query = join.generateJoin(query)
  }

  return query
}

private fun executeQuery(
  dsl: DSLContext,
  tableGraphNode: TableNode,
  query: SqlQuery
): List<Pair<Record, Result<Record>>> {
  val rootTable = tableGraphNode.data
  val queryResult = dsl.fetch(query)
  val resultGroupedByRootPK = queryResult.intoGroups(rootTable)
  return resultGroupedByRootPK.toList()
}

private fun isFkField(selectedField: SelectedField) =
  !GraphQLTypeUtil.isLeaf(selectedField.fieldDefinition.type)

private fun graphQlTypeFromTable(tableGraphNode: TableNode): GraphQLObjectType {
  val table = tableGraphNode.data
  val typeBuilder = GraphQLObjectType
    .newObject()
    .name(table.name)

  for (field in table.fields()) {
    typeBuilder.field { f ->
      f.type(sqlTypeToGraphQlType(field.dataType))
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

private fun sqlTypeToGraphQlType(type: DataType<out Any>): GraphQLOutputType {
  return when (type) {
    SQLDataType.UUID -> Scalars.GraphQLID
    SQLDataType.VARCHAR -> Scalars.GraphQLString
    SQLDataType.BIGINT -> Scalars.GraphQLLong
    else -> Scalars.GraphQLString
  }
}
