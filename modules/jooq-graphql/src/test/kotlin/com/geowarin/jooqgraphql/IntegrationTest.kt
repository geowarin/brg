package com.geowarin.jooqgraphql

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

class PgContainer: PostgreSQLContainer<PgContainer>("postgres:10-alpine")

@Testcontainers
class IntegrationTest {
  @Container
  private val postgreSQLContainer = PgContainer()

  lateinit var jooq: DSLContext

  @BeforeEach
  internal fun setUp() {
    jooq = DSL.using(postgreSQLContainer.jdbcUrl, postgreSQLContainer.username, postgreSQLContainer.password)
    postSchema.executeDDL(jooq)
  }

  @Test
  fun `retrieve join results`() {
    val personId = UUID.randomUUID()

    jooq.insertInto(personTable)
      .columns(
        personTable.get<UUID>("id"),
        personTable.get<String>("first_name")
      )
      .values(personId, "toto")
      .execute()

    jooq.insertInto(postTable)
      .columns(
        postTable.get<UUID>("id"),
        postTable.get<String>("headline"),
        postTable.get<UUID>("person_id")
      )
      .values(
        UUID.randomUUID(), "title", personId
      )
      .execute()

    val jsonResult = postSchema.executeGraphqlQuery(
      jooq,"""{
        post {
          headline
          
          persons {
            first_name
          }
        }
      }"""
    )

    println(jsonResult)
  }
}