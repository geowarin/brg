package com.geowarin.brg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.introspection.IntrospectionQuery
import graphql.introspection.IntrospectionQuery.INTROSPECTION_QUERY
import graphql.introspection.IntrospectionResultToSchema
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.kotlin.test.test

@SpringBootTest(webEnvironment = RANDOM_PORT)
class BrgApplicationTests(
  @LocalServerPort port: Int,
  @Autowired val graphQL: GraphQL
) {
  private val client = WebClient.create("http://localhost:$port")

  @Test
  fun `should get schema`() {
    val schemaResult = graphQL.execute(ExecutionInput.newExecutionInput().query(INTROSPECTION_QUERY).build())
    val schema = IntrospectionResultToSchema().createSchemaDefinition(schemaResult)
    println(schema)
  }

  @Test
  fun `should retrieve users`() {
    client.graphqlQuery("{brg_user{email}}") {
      it.isJsonEqual(
        """
{
    "data": {
      "brg_user": [
        {
          "email": "tata"
        }
      ]
    }
  }
"""
      )
    }
  }
}

fun WebClient.graphqlQuery(@Language("graphql") query: String, assertion: (String) -> Unit) {
  this.post().uri("/graphql")
    .bodyValue(GraphQLParameters(query = query))
    .retrieve()
    .bodyToMono<String>()
    .test()
    .consumeNextWith(assertion)
    .verifyComplete()
}


fun String.isJsonEqual(@Language("json") expected: String) {
  val result = JSONCompare.compareJSON(this, expected, JSONCompareMode.LENIENT)
  if (result.failed()) {
    Assertions.assertEquals(prettyJSON(expected), prettyJSON(this))
  }
}

fun prettyJSON(jsonObject: String): String {
  val objectMapper = ObjectMapper()
  return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readValue(jsonObject))
}

data class GraphQLParameters(
  val query: String
)

//data class GraphqlResponse<out T>(
//        val data: T,
//        val errors: List<Any>,
//        val extensions: Any?
//)