package com.geowarin.jooqgraphql

import org.jooq.ForeignKey
import org.jooq.Table
import java.util.*

typealias Data = Table<*>
typealias Dep = ForeignKey<*,*>

class TableNode(
  val data: Data,
  internal val dependencyFks: HashSet<Dep> = hashSetOf(),
  internal val dependantFks: HashSet<Dep> = hashSetOf()
) {
  override fun equals(other: Any?): Boolean = other is Node<*> && data == other.data
  override fun hashCode(): Int = data.hashCode()
  override fun toString(): String = data.toString()

  val dependencies
    get() = dependencyFks.map { fkToTable(it) }

  val dependants
    get() = dependantFks.map { fkToOriginTable(it) }
}

fun fkToTable(fk: Dep): Data {
  return fk.key.table
}
fun fkToOriginTable(fk: Dep): Data {
  return fk.table
}

class TableDependencyGraph {
  private val nodes = hashSetOf<TableNode>()

  fun addDependencies(from: Data, vararg dependencies: Dep) {
    val fromNode = findOrAddNode(from)
    for (to in dependencies) {
      addDependency(fromNode, to)
    }
  }

  private fun addDependency(fromNode: TableNode, to: Dep) {

    fromNode.dependencyFks.add(to)

    val toTable = fkToTable(to)
    val toNode = findOrAddNode(toTable)
    toNode.dependantFks.add(to)
  }

  private fun findOrAddNode(dataNode: Data): TableNode {
    val node = nodes.find { it.data == dataNode }
    if (node != null)
      return node
    val newNode = TableNode(data = dataNode)
    nodes.add(newNode)
    return newNode
  }

  fun getNode(data: Data): TableNode = nodes.find { it.data == data }
    ?: throw IllegalArgumentException("Could not find node $data")
}

