package com.geowarin.jooqgraphql

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.StringAssert

class SqlAssert(value: String) : AbstractAssert<StringAssert, String>(value, SqlAssert::class.java) {
  fun isEqualTo(expected: String): SqlAssert {
    Assertions.assertThat(actual.trim()).isEqualToIgnoringWhitespace(expected)
    return this
  }

  companion object {
    fun assertThatSql(value: String): SqlAssert {
      return SqlAssert(value)
    }
  }
}