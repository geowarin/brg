package codegen

import org.jooq.*
import org.jooq.impl.Internal
import org.jooq.impl.Internal.createUniqueKey

fun createKey(table: Table<Record>, vararg field: TableField<Record, *>): UniqueKey<Record> =
  createUniqueKey(table, *field)
fun createFk(key: UniqueKey<Record>, table: Table<Record>, field: TableField<Record, *>): ForeignKey<Record, Record> =
  Internal.createForeignKey<Record, Record>(key, table, "fk_${key.name}", field)
