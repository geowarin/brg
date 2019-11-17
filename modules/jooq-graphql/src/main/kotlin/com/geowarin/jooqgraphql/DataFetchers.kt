package com.geowarin.jooqgraphql

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLObjectType
import graphql.schema.SelectedField
import org.jooq.*
import java.util.*

typealias TableDataFetcher = (Table<*>, DataFetchingEnvironment) -> Iterable<Record>
typealias QueryGenerator = (DSLContext, Table<*>, DataFetchingEnvironment) -> SelectFinalStep<Record>

object DataFetchers {
  val NULL: TableDataFetcher = { _, _ -> emptyList() }

  val DEFAULT: (jooq: DSLContext) -> TableDataFetcher = { jooq ->
    { table, e ->
      val query = DEFAULT_QUERY_GENERATOR(jooq, table, e)
      query.fetch()
    }
  }

  // e.selectionSet.fields[2].selectionSet empty pour les feuilles
  // e.selectionSet.fields[1].selectionSet.definitions contient une map<path, Field>
  // e.selectionSet.getField("persons").selectionSet.definitions => [fist_name = Field@xxx, ...]
  // e.selectionSet.definitions["persons"].type is GraphQLObjectType
  // e.selectionSet.definitions["persons/first_name"].type is GraphQLScalarType
  // e.selectionSet.getFields("persons/*") renvoie toutes les fields liées à persons
  // e.selectionSet.getFields("*") renvoie les fields du root (pas persons/xxx)

  data class Joining(
    val foreignKey: ForeignKey<out Record, out Record>,
    val fields: List<Field<*>>
  )

  private fun findFk(table: Table<out Record>, fkGraphqlField: SelectedField): ForeignKey<out Record, out Record> {
    return table.references.find { it.key.table.name == fkGraphqlField.name.removeSuffix("s") }
      ?: throw IllegalStateException("Could not find fk for field ${fkGraphqlField.name}")
  }

  val DEFAULT_QUERY_GENERATOR: QueryGenerator = { jooq, table, e ->
    val rootFields = e.selectionSet.getFields("*")
    val (fkGraphqlFields, scalarFields) = rootFields.partition { it.fieldDefinition.type is GraphQLObjectType }

//    val fkJooqFields = table.references.map { it.fields.first() }
    val joinings = fkGraphqlFields.map { fkGraphqlField ->

      val foreignKey = findFk(table, fkGraphqlField)

      val subGraphqlFields = e.selectionSet.getFields("${fkGraphqlField.name}/*")
      val fkTable = foreignKey.key.table
      val subFields = subGraphqlFields.map { fkTable.field(it.name) }

      Joining(foreignKey = foreignKey, fields = subFields)
    }

    val sqlFields = scalarFields.map { table.field(it.name) } + joinings.flatMap { it.fields }

    var query = jooq.select(sqlFields).from(table)
    joinings.forEach { joinig ->
      val foreignKey = joinig.foreignKey
      query = query.join(foreignKey.key.table).on(getJoinCondition(joinig.foreignKey))
    }

    query
  }

  private fun getJoinCondition(foreignKey: ForeignKey<out Record, out Record>): Condition {
    val fkFields = foreignKey.key.fields
    val localFields = foreignKey.fields

    val localField = localFields.first() as Field<UUID>
    val fkField = fkFields.first() as Field<UUID>
    return fkField.equal(localField)
  }
}

