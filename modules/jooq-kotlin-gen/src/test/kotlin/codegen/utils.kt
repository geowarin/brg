package org.jooq.impl

import org.jooq.*

fun createKey(table: Table<Record>, vararg field: TableField<Record, *>): UniqueKey<Record> = UniqueKeyImpl<Record>(table, *field)
fun createFk(key: UniqueKey<Record>, table: Table<Record>, field: TableField<Record, *>): ForeignKey<Record, Record> = ReferenceImpl<Record, Record>(key, table, field)
