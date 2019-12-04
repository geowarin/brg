package com.geowarin.jooqgraphql

import com.geowarin.jooqgraphql.misc.Node
import graphql.schema.SelectedField
import org.jooq.*
import org.jooq.impl.DSL
import java.util.*

typealias Data = Table<*>
typealias FK = ForeignKey<*, *>

class Join(
  val foreignKey: FK,
  val subGraphqlFields: List<SelectedField>,
  val reverse: Boolean = false
) {
  private val fkTable = if (reverse) foreignKey.table else foreignKey.key.table
  val sqlFields = subGraphqlFields.map { fkTable.field(it.name) }

  fun generateJoin(query: SqlQuery): SqlQuery {
    return query.join(fkTable).on(foreignKey.getJoinCondition(reverse))
  }
}

fun FK.getJoinCondition(reverse: Boolean = false): Condition {
  val initial: Condition = DSL.noCondition()
  val fieldsPair = if (reverse) fields.zip(key.fields) else key.fields.zip(fields)
  return fieldsPair
    .fold(initial) { acc, (f1, f2) ->
      @Suppress("UNCHECKED_CAST")
      acc.and((f1 as Field<Nothing>).equal(f2 as Field<Nothing>))
    }
}

class TableNode(
  val data: Data,
  internal val dependencyFks: HashSet<FK> = hashSetOf(),
  internal val dependantFks: HashSet<FK> = hashSetOf()
) {
  override fun equals(other: Any?): Boolean = other is Node<*> && data == other.data
  override fun hashCode(): Int = data.hashCode()
  override fun toString(): String = data.toString()

  val dependencies
    get() = dependencyFks.map { fkToTable(it) }

  val dependants
    get() = dependantFks.map { fkToOriginTable(it) }
}

fun fkToTable(fk: FK): Data {
  return fk.key.table
}

fun fkToOriginTable(fk: FK): Data {
  return fk.table
}

class TableDependencyGraph {
  private val nodes = hashSetOf<TableNode>()

  fun addDependencies(from: Data, vararg dependencies: FK) {
    val fromNode = findOrAddNode(from)
    for (to in dependencies) {
      addDependency(fromNode, to)
    }
  }

  private fun addDependency(fromNode: TableNode, to: FK) {
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
