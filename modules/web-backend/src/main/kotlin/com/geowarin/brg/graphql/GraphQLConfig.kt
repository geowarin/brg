package com.geowarin.brg.graphql

import com.geowarin.model.brg_security.Tables
import graphql.GraphQL
import graphql.Scalars.*
import graphql.schema.*
import org.jooq.DataType
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.SQLDataType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfig(
  val dataFetcherFactory: TableDataFetcher
) : GraphQLFactory {

  @Bean
  override fun makeGraphQL(): GraphQL = GraphQL.newGraphQL(buildSchema()).build()

  fun buildSchema(): GraphQLSchema = GraphQLSchema.newSchema()
    .query(
      GraphQLObjectType.newObject()
        .name("query")
        .field(queryFromTable(Tables.BRG_USER))
    ).build()

  private fun <T : Record> queryFromTable(table: Table<T>): GraphQLFieldDefinition.Builder {
    return GraphQLFieldDefinition.newFieldDefinition()
      .name(table.name)
      .type(GraphQLList.list(graphQlTypeFromTable(table)))
      .dataFetcher { e -> dataFetcherFactory.fetch(table, e) }
  }

  fun <T : Record> graphQlTypeFromTable(table: Table<T>): GraphQLObjectType {
    val typeBuilder = GraphQLObjectType
      .newObject()
      .name(table.name)

    for (field in table.fields()) {
      typeBuilder.field { f ->
        f.type(getType(field.dataType))
          .name(field.name)
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