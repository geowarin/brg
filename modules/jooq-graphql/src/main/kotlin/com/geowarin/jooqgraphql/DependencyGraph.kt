package com.geowarin.jooqgraphql

import java.util.*

class Node<T : Any>(
  val data: T,
  internal val dependencyNodes: HashSet<Node<T>> = hashSetOf(),
  internal val dependantNodes: HashSet<Node<T>> = hashSetOf()
) {
  override fun equals(other: Any?): Boolean = other is Node<*> && data == other.data
  override fun hashCode(): Int = data.hashCode()
  override fun toString(): String = data.toString()

  val dependencies
    get() = dependencyNodes.map { it.data }

  val dependants
    get() = dependantNodes.map { it.data }
}

class DependencyGraph<T : Any> {
  private val nodes = hashSetOf<Node<T>>()

  fun addDependencies(from: T, vararg dependencies: T) {
    val fromNode = findOrAddNode(from)
    for (to in dependencies) {
      addDependency(fromNode, to)
    }
  }

  private fun addDependency(fromNode: Node<T>, to: T) {
    val toNode = findOrAddNode(to)
    fromNode.dependencyNodes.add(toNode)
    toNode.dependantNodes.add(fromNode)
  }

  private fun findOrAddNode(dataNode: T): Node<T> {
    val node = nodes.find { it.data == dataNode }
    if (node != null)
      return node
    val newNode = Node(data = dataNode)
    nodes.add(newNode)
    return newNode
  }

  fun topologicalSort(): List<Node<T>> {
    checkNoCycles(this.nodes)
    return topologicalSort(this.nodes)
  }

  fun getNode(data: T): Node<T> = nodes.find { it.data == data }
    ?: throw IllegalArgumentException("Could not find node $data")
}

enum class Color {
  GRAY,
  WHITE,
  BLACK
}

fun <T : Any> checkNoCycles(nodes: Iterable<Node<T>>) {
  val colors = hashMapOf<Node<T>, Color>()

  fun visit(node: Node<T>, path: LinkedList<Node<T>>) {
    path.add(node)
    colors[node] = Color.GRAY

    for (dependency in node.dependencyNodes) {
      if (colors[dependency] == Color.WHITE) {
        visit(dependency, path)
      } else if (colors[dependency] == Color.GRAY) {
        throw IllegalStateException("Cycle detected: ${path.joinToString("->")}->$dependency")
      }
    }
    colors[node] = Color.BLACK
  }

  for (node in nodes) {
    colors[node] = Color.WHITE
  }

  for (node in nodes) {
    if (colors[node] == Color.WHITE) {
      visit(node, LinkedList())
    }
  }
}

fun <T : Any> topologicalSort(nodes: Iterable<Node<T>>): List<Node<T>> {
  val results = LinkedList<Node<T>>()
  fun visited(node: Node<T>) = results.contains(node)

  fun visit(node: Node<T>) {
    if (visited(node))
      return

    for (dependency in node.dependencyNodes) {
      visit(dependency)
    }

    results.addFirst(node)
  }

  for (node in nodes) {
    visit(node)
  }

  return results
}
