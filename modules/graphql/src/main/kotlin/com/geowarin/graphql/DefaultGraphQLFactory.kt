package com.geowarin.graphql

import com.geowarin.model.brg_security.Tables
import graphql.GraphQL
import graphql.Scalars.*
import graphql.schema.*
import org.jooq.DataType
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.SQLDataType

class DefaultGraphQLFactory(
  private val dataFetcherFactory: TableDataFetcher
) : GraphQLFactory {

  override fun makeGraphQL(): GraphQL = GraphQL.newGraphQL(buildSchema()).build()

  private fun buildSchema(): GraphQLSchema = GraphQLSchema.newSchema()
    .query(
      GraphQLObjectType.newObject()
        .name("QueryType")
        .field(queryFromTable(Tables.BRG_USER))
    ).build()

  private fun <T : Record> queryFromTable(table: Table<T>): GraphQLFieldDefinition.Builder {
    return GraphQLFieldDefinition.newFieldDefinition()
      .name(table.name)
      .type(GraphQLList.list(graphQlTypeFromTable(table)))
      .description(table.comment)
      .dataFetcher { e -> dataFetcherFactory.fetch(table, e) }
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
    return typeBuilder.build()
  }

  private fun getType(type: DataType<out Any>): GraphQLOutputType {
    return when (type) {
      SQLDataType.UUID -> GraphQLID
      SQLDataType.VARCHAR -> GraphQLString
      SQLDataType.BIGINT -> GraphQLLong
      else -> GraphQLString
    }
  }
}