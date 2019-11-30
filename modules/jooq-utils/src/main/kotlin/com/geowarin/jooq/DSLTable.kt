package com.geowarin.jooq

import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl
import java.lang.IllegalArgumentException

data class DslField<T>(
  val name: String,
  val dataType: DataType<*>,
  val pk: Boolean = false
) {
  internal var fk: UniqueKey<Record>? = null
}

infix fun <T> DslField<T>.fkOn(table: Table<Record>): DslField<T> {
  this.fk = table.primaryKey
  return this
}

@DslMarker
annotation class TableDsl

@TableDsl
class DSLTable(val name: String) {
  val fields: MutableList<DslField<*>> = mutableListOf()

  fun <T> field(name: String, dataType: DataType<T>): DslField<T> {
    val dslField = DslField<T>(name, dataType)
    fields.add(dslField)
    return dslField
  }

  fun <T> pk(name: String, dataType: DataType<T>): DslField<T> {
    val dslField = DslField<T>(name, dataType, pk = true)
    fields.add(dslField)
    return dslField
  }

  fun getTable(): MyTable {
    val pks: MutableList<TableField<Record, *>> = mutableListOf()
    val fks: MutableList<Pair<UniqueKey<Record>, TableField<Record, *>>> = mutableListOf()

    class Table : MyTable(name) {
      init {
        for (field in fields) {
          addField(field)
        }
      }

      private fun addField(field: DslField<*>) {
        val jooqField = createField(DSL.name(field.name), field.dataType)
        if (field.pk) {
          pks.add(jooqField)
        }
        if (field.fk != null) {
          fks.add(field.fk!! to jooqField)
        }
      }

      val key = if (pks.isEmpty()) null else createKey(*pks.toTypedArray())
      val foreignKeys = createFks(*fks.toTypedArray())
      val keyFields: List<UniqueKey<Record>?> = listOf(key)

      override fun getPrimaryKey(): UniqueKey<Record>? = key
      override fun getKeys() = keyFields
      override fun getReferences(): List<ForeignKey<Record, Record>> = foreignKeys
    }
    return Table()
  }
}

open class MyTable(name: String) : TableImpl<Record>(DSL.name(name)) {
  operator fun <T> get(fieldName: String): Field<T> {
    return field(fieldName) as Field<T>? ?: throw IllegalArgumentException("Non existing field $fieldName on table ${this.name}")
  }
}

fun table(name: String = "table", init: DSLTable.() -> Unit): MyTable = DSLTable(name).apply(init).getTable()
