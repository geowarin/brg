package com.geowarin.brg

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.kotlin.test.test

@SpringBootTest(webEnvironment = RANDOM_PORT)
class BrgApplicationTests(@LocalServerPort port: Int) {
    private val client = WebClient.create("http://localhost:$port")

    @Test
    fun `should handle post`() {
        client.post().uri("/graphql")
                .bodyValue(GraphQLParameters(query = "{brg_user{email}}"))
                .retrieve()
                .bodyToMono<String>()
                .test()
                .consumeNextWith {
                    it.isJsonEqual("""{
                                  "data": {
                                    "brg_user": [
                                      {
                                        "email": "tat"
                                      }
                                    ]
                                  }
                                }
                     """)
                }
                .verifyComplete()
    }
}

fun String.isJsonEqual(@Language("json") other: String) {
    val result = JSONCompare.compareJSON(this, other, JSONCompareMode.LENIENT)
    if (result.failed()) {
        assertThat(this).isEqualTo(other)
//        Assertions.assertEquals(other, this)
    }
}

data class GraphQLParameters(
        val query: String
)

//data class GraphqlResponse<out T>(
//        val data: T,
//        val errors: List<Any>,
//        val extensions: Any?
//)