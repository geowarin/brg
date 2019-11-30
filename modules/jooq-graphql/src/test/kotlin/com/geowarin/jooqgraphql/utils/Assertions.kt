package com.geowarin.jooqgraphql.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode

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
