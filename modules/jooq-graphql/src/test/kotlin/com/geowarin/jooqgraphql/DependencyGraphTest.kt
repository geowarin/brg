package com.geowarin.jooqgraphql

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DependencyGraphTest {

  @Test
  fun testGraph() {
    val dependencyGraph = DependencyGraph<Int>()
    dependencyGraph.addDependencies(5, 2, 0)
    dependencyGraph.addDependencies(4, 0, 1)
    dependencyGraph.addDependencies(2, 3)
    dependencyGraph.addDependencies(3, 1)

    assertThat(dependencyGraph.topologicalSort().map { it.data })
      .containsExactly(5, 4, 2, 3, 1, 0)
  }

  @Test
  fun testCycle() {
    val dependencyGraph = DependencyGraph<Int>()
    dependencyGraph.addDependencies(5, 2)
    dependencyGraph.addDependencies(2, 3)
    dependencyGraph.addDependencies(3, 5)

    val exception = assertThrows<IllegalStateException> {
      dependencyGraph.topologicalSort()
    }
    assertThat(exception.message).isEqualTo("Cycle detected: 2->3->5->2")
  }
}
