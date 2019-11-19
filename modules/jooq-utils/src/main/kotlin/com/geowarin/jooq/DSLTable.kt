package com.geowarin.jooq

import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

data class DslField(
  val name: String,
  val dataType: DataType<*>,
  val pk: Boolean = false
) {
  internal var fk: UniqueKey<Record>? = null

  infix fun fkOn(table: Table<Record>) {
    this.fk = table.primaryKey
  }
}

@DslMarker
annotation class TableDsl

@TableDsl
class DSLTable(val name: String) {
  val fields: MutableList<DslField> = mutableListOf()

  fun field(name: String, dataType: DataType<*>): DslField {
    val dslField = DslField(name, dataType)
    fields.add(dslField)
    return dslField
  }

  fun pk(name: String, dataType: DataType<*>): DslField {
    val dslField = DslField(name, dataType, pk = true)
    fields.add(dslField)
    return dslField
  }

  fun getTable(): Table<Record> {
    val pks: MutableList<TableField<Record, *>> = mutableListOf()
    val fks: MutableList<Pair<UniqueKey<Record>, TableField<Record, *>>> = mutableListOf()

    class Table : TableImpl<Record>(DSL.name(name)) {
      init {
        for (field in fields) {
          val jooqField = createField(DSL.name(field.name), field.dataType)
          if (field.pk) {
            pks.add(jooqField)
          }
          if (field.fk != null) {
            fks.add(field.fk!! to jooqField)
          }
        }
      }

      val key = if (pks.isEmpty()) null else createKey(*pks.toTypedArray())
      val foreignKeys = createFks(*fks.toTypedArray())

      override fun getPrimaryKey(): UniqueKey<Record>? = key

      override fun getKeys() = listOf(key)

      override fun getReferences(): List<ForeignKey<Record, Record>> = foreignKeys
    }
    return Table()
  }
}

fun table(name: String = "table", init: DSLTable.() -> Unit): Table<Record> {
  val tableDsl = DSLTable(name)
  init(tableDsl)
  return tableDsl.getTable()
}