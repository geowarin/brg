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
class DSLTable {
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
    class Table : TableImpl<Record>(DSL.name("person")) {
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

      override fun getPrimaryKey(): UniqueKey<Record> {
        return createKey(*pks.toTypedArray())
      }

      override fun getReferences(): List<ForeignKey<Record, Record>> {
        return createFks(*fks.toTypedArray())
      }
    }
    return Table()
  }
}

fun table(init: DSLTable.() -> Unit): Table<Record> {
  val tableDsl = DSLTable()
  init(tableDsl)
  return tableDsl.getTable()
}