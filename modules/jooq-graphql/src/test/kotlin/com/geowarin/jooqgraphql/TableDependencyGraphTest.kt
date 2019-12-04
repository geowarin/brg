package com.geowarin.jooqgraphql

import com.geowarin.jooq.fkOn
import com.geowarin.jooq.table
import com.geowarin.jooqgraphql.utils.personTable
import com.geowarin.jooqgraphql.utils.postTable
import org.assertj.core.api.Assertions.assertThat
import org.jooq.Field
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TableDependencyGraphTest {

  @Nested
  inner class JoinCondition {
    @Test
    fun `simple fk`() {
      val personTable = table("person") {
        pk("id", SQLDataType.UUID)
      }

      val postTable = table("post") {
        pk("id", SQLDataType.UUID)
        field("person_id", SQLDataType.UUID) fkOn personTable
      }

      val references: List<FK> = postTable.references
      assertThat(references.map { it.getJoinCondition().toString() })
        .containsExactly(""""person"."id" = "post"."person_id"""")
    }
  }

  @Test
  fun toto() {
    val tableDependencyGraph = TableDependencyGraph()
    listOf(
      personTable,
      postTable
    ).map { table ->
      tableDependencyGraph.addDependencies(table, *table.references.toTypedArray())
    }
    println(tableDependencyGraph)

    val postTableNode = tableDependencyGraph.getNode(postTable)
    assertThat(postTableNode.dependencies)
      .containsExactly(personTable)

    assertThat(postTableNode.dependencyFks.flatMap { fk -> fk.fields.map { it.qualified() } })
      .containsExactly("post.person_id")
    assertThat(postTableNode.dependencyFks.flatMap { fk -> fk.key.fields.map { it.qualified() } })
      .containsExactly("person.id")

    val personTableNode = tableDependencyGraph.getNode(personTable)
    assertThat(personTableNode.dependants)
      .containsExactly(postTable)

    assertThat(personTableNode.dependantFks.flatMap { fk -> fk.fields.map { it.qualified() } })
      .containsExactly("post.person_id")
    assertThat(personTableNode.dependantFks.flatMap { fk -> fk.key.fields.map { it.qualified() } })
      .containsExactly("person.id")
  }

  private fun Field<*>.qualified() = qualifiedName.toString().replace("\"", "")
}
