package com.geowarin.brg

import com.geowarin.model.brg_security.Tables
import graphql.GraphQL
import graphql.Scalars.*
import graphql.schema.*
import org.jooq.DSLContext
import org.jooq.DataType
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.SQLDataType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class GraphQlConfig(
  val jooq: DSLContext
) {

  @Bean
  fun graphQl(): GraphQL {
    return GraphQL.newGraphQL(buildSchema()).build()
  }

  fun buildSchema(): GraphQLSchema {

    return GraphQLSchema.newSchema()
      .query(
        GraphQLObjectType.newObject()
          .name("query")
          .field(queryFromTable(Tables.BRG_USER))
      ).build()
  }

  fun <T: Record> graphQlTypeFromTable(table: Table<T>): GraphQLObjectType {
    val typeBuilder = GraphQLObjectType.newObject()
      .name(table.name)
    table.fields().forEach { field ->
      typeBuilder.field { f ->
        val field: GraphQLFieldDefinition.Builder = f.type(getType(field.dataType)).name(field.name)
                .dataFetcher {
                  val source: T = it.getSource()
                  source.get(it.field.name)
                }
        field
      }
    }
    return typeBuilder
      .build()
  }

  private fun getType(type: DataType<out Any>): GraphQLOutputType {
    return when (type) {
      SQLDataType.UUID -> GraphQLID
      SQLDataType.VARCHAR -> GraphQLString
      SQLDataType.BIGINT -> GraphQLLong
      else -> GraphQLString
    }
  }

  private fun <T: Record> queryFromTable(table: Table<T>): GraphQLFieldDefinition.Builder {
    val users = graphQlTypeFromTable(table)

    return GraphQLFieldDefinition.newFieldDefinition()
      .name(table.name)
      .type(GraphQLList.list(users))
      .dataFetcher {
        jooq.select().from(table).fetch()
      }
  }
}