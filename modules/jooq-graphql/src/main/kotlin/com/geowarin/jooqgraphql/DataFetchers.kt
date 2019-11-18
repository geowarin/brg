package com.geowarin.jooqgraphql

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.SelectedField
import org.jooq.*

typealias TableDataFetcher = (TableGraphNode, DataFetchingEnvironment) -> Iterable<Record>
typealias QueryGenerator = (DSLContext, TableGraphNode, DataFetchingEnvironment) -> SelectFinalStep<Record>

data class Join(
  val foreignKey: Fk,
  val subGraphqlFields: MutableList<SelectedField>
) {
  val fkTable = foreignKey.foreignTable
  val subFields = subGraphqlFields.map { fkTable.field(it.name) }

  fun getJoinCondition(): Condition {
    val fkFields = foreignKey.foreignFields as List<Field<Any?>>
    val localFields = foreignKey.localFields as List<Any>

    val localField = localFields.first()
    val fkField = fkFields.first()
    return fkField.equal(localField)
  }
}

object DataFetchers {
  val NULL: TableDataFetcher = { _, _ -> emptyList() }

  val DEFAULT: (jooq: DSLContext) -> TableDataFetcher = { jooq ->
    { tableGraphNode, e ->
      val query = DEFAULT_QUERY_GENERATOR(jooq, tableGraphNode, e)
      query.fetch()
    }
  }

  private fun findFk(tableGraphNode: TableGraphNode, fkGraphqlField: SelectedField): Fk {
    return tableGraphNode.foreignKeys.find { it.foreignTable.name == fkGraphqlField.name.removeSuffix("s") }
      ?: tableGraphNode.reverseForeignKeys.find { it.foreignTable.name == fkGraphqlField.name.removeSuffix("s") }
      ?: throw IllegalStateException("Could not find fk for field ${fkGraphqlField.name}")
  }

  val DEFAULT_QUERY_GENERATOR: QueryGenerator = { jooq, tableGraphNode, e ->
    val table = tableGraphNode.table
    val rootFields = e.selectionSet.getFields("*")
    val (fkGraphqlFields, scalarFields) = rootFields.partition {
      val type = it.fieldDefinition.type
      type is GraphQLList && type.wrappedType is GraphQLObjectType
    }

    val joins = fkGraphqlFields.map { fkGraphqlField ->
      val foreignKey = findFk(tableGraphNode, fkGraphqlField)
      val subGraphqlFields = e.selectionSet.getFields("${fkGraphqlField.name}/*")

      Join(foreignKey = foreignKey, subGraphqlFields = subGraphqlFields)
    }

    val sqlFields = scalarFields.map { table.field(it.name) } + joins.flatMap { it.subFields }

    var query = jooq.select(sqlFields).from(table)
    joins.forEach { join ->
      query = query.join(join.fkTable).on(join.getJoinCondition())
    }

    query
  }
}

