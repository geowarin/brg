package com.geowarin.jooqgraphql

import com.geowarin.jooq.table
import com.geowarin.jooqgraphql.utils.SqlAssert.Companion.assertThatSql
import com.geowarin.jooqgraphql.utils.TestGraphQlSchema
import com.geowarin.jooqgraphql.utils.postSchema
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test

internal class QueryGenerationTest {

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

  @Test
  fun `fk direct way`() {
    val query = postSchema.getSqlQuery(
      """{
        post {
          headline
          
          person {
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

  @Test
  fun `cycle back to first table`() {
    postSchema.print()
    val query = postSchema.getSqlQuery(
      """{
        person {
          first_name
          
          posts {
            headline
            person {
              last_name
            }
          }
        }
      }"""
    )
    assertThatSql(query).isEqualTo(
      """
      select 
        person.first_name,
        person.last_name,
        post.headline 
      from person
        join post
          on post.person_id = person.id
      """
    )
  }
}

