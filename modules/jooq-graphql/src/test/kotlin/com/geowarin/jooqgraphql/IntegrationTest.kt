package com.geowarin.jooqgraphql

import com.geowarin.jooqgraphql.utils.Containers.pgContainer
import com.geowarin.jooqgraphql.utils.isJsonEqual
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
class IntegrationTest {
  lateinit var jooq: DSLContext

  @BeforeAll
  internal fun setUp() {
    jooq = DSL.using(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password)
    postSchema.executeDDL(jooq)

    val personId = UUID.randomUUID()

    jooq.insertInto(personTable)
      .columns(personTable.get<UUID>("id"), personTable.get<String>("first_name"))
      .values(personId, "toto")
      .execute()

    jooq.insertInto(postTable)
      .columns(postTable.get<UUID>("id"), postTable.get<String>("headline"), postTable.get<UUID>("person_id"))
      .values(UUID.randomUUID(), "first toto post", personId)
      .execute()

    jooq.insertInto(postTable)
      .columns(postTable.get<UUID>("id"), postTable.get<String>("headline"), postTable.get<UUID>("person_id"))
      .values(UUID.randomUUID(), "second toto post", personId)
      .execute()
  }

  @Test
  fun `retrieve single table result`() {
    postSchema.executeGraphqlQuery(
      jooq, """{
        post {
          headline
        }
      }"""
    ).isJsonEqual(
      """{
  "data": {
    "post": [
      {
        "headline": "first toto post"
      }, {
        "headline": "second toto post"
      }
    ]
  }
}"""
    )
  }

  @Test
  fun `retrieve join results direct FK`() {
    postSchema.executeGraphqlQuery(
      jooq, """{
        post {
          headline
          
          person {
            first_name
          }
        }
      }"""
    ).isJsonEqual(
      """{
  "data": {
    "post": [
      {
        "headline": "first toto post",
        "person": {
            "first_name": "toto"
          }
        }, {
        "headline": "second toto post",
        "person": {
          "first_name": "toto"
        }
      }
    ]
  }
}"""
    )
  }

  @Test
  fun `retrieve join results reverse FK`() {
    postSchema.executeGraphqlQuery(
      jooq, """{
        person {
          first_name
          
          posts {
            headline
          }
        }
      }"""
    ).isJsonEqual(
      """{
  "data": {
    "person": [
      {
        "first_name": "toto",
        "posts": [
          {
            "headline": "first toto post"
          }, {
            "headline": "second toto post"
          }
        ]
      }
    ]
  }
}"""
    )
  }
}
