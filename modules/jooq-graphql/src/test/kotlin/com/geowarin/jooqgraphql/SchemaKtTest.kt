package com.geowarin.jooqgraphql

import com.geowarin.jooq.table
import com.geowarin.jooqgraphql.SqlAssert.Companion.assertThatSql
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test

internal class SchemaKtTest {

  @Test
  fun `select one field`() {
    val personTable = table("person") {
      field("first_name", SQLDataType.VARCHAR)
    }
    val schema = TestGraphQlSchema(personTable)

    val query = schema.getSqlQuery(
      """{
        person {
          first_name
        }
      }"""
    )
    assertThatSql(query).isEqualTo(
      """
      select person.first_name from person
      """
    )
  }

  val personTable = table("person") {
    pk("id", SQLDataType.UUID)
    field("first_name", SQLDataType.VARCHAR)
    field("last_name", SQLDataType.VARCHAR)
    field("about", SQLDataType.CLOB)
    field("createdAt", SQLDataType.TIMESTAMP)
  }
  val postTable = table("post") {
    pk("id", SQLDataType.UUID)
    field("person_id", SQLDataType.UUID) fkOn personTable
    field("headline", SQLDataType.CLOB)
    field("body", SQLDataType.CLOB)
    field("topic", SQLDataType.VARCHAR)
    field("createdAt", SQLDataType.TIMESTAMP)
  }

  val postSchema = TestGraphQlSchema(personTable, postTable)

  @Test
  fun `fk direct way`() {
    val query = postSchema.getSqlQuery(
      """{
        post {
          headline
          
          persons {
            first_name
          }
        }
      }"""
    )
    assertThatSql(query).isEqualTo(
      """
      select 
        post.headline, 
        person.first_name
      from post
        join person
          on person.id = post.person_id
      """
    )
  }

  @Test
  fun `fk reverse way`() {
    postSchema.print()
    val query = postSchema.getSqlQuery(
      """{
        person {
          first_name
          
          posts {
            headline
          }
        }
      }"""
    )
    assertThatSql(query).isEqualTo(
      """
      select 
        person.first_name,
        post.headline 
      from person
        join post
          on post.person_id = person.id
      """
    )
  }
}

