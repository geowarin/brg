package com.geowarin.jooqgraphql

import org.assertj.core.api.Assertions.assertThat
import org.jooq.Field
import org.junit.jupiter.api.Test

internal class TableNodeTest {

  @Test
  fun toto() {
    val tableDependencyGraph = TableDependencyGraph()
    listOf(personTable, postTable).map { table ->
      tableDependencyGraph.addDependencies(table, *table.references.toTypedArray())
    }
    println(tableDependencyGraph)

    val postTableNode = tableDependencyGraph.getNode(postTable)
    assertThat(postTableNode.dependencies)
      .containsExactly(personTable)

    assertThat(postTableNode.dependencyFks.flatMap { fk -> fk.localFields.map { it.qualified() } })
      .containsExactly("post.person_id")
    assertThat(postTableNode.dependencyFks.flatMap { fk -> fk.foreignFields.map { it.qualified() } })
      .containsExactly("person.id")

    val personTableNode = tableDependencyGraph.getNode(personTable)
    assertThat(personTableNode.dependants)
      .containsExactly(postTable)

    assertThat(personTableNode.dependantFks.flatMap { fk -> fk.localFields.map { it.qualified() } })
      .containsExactly("post.person_id")
    assertThat(personTableNode.dependantFks.flatMap { fk -> fk.foreignFields.map { it.qualified() } })
      .containsExactly("person.id")
  }

  private fun Field<*>.qualified() = qualifiedName.toString().replace("\"", "")
}
