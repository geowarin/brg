package codegen

import com.squareup.kotlinpoet.FileSpec
import org.intellij.lang.annotations.Language
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType.UUID
import org.jooq.impl.SQLDataType.VARCHAR
import org.jooq.impl.TableImpl
import org.jooq.impl.createFk
import org.jooq.impl.createKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinTableFunctionsGeneratorKtTest {

  @Test
  fun `should generate new record function with a non-nullable string argument`() {

    @Suppress("unused")
    class PersonTable : TableImpl<Record>(DSL.name("PERSON")) {
      val firstName = this.createField("FIRST_NAME", VARCHAR.length(255).nullable(false))
    }

    generateNewFunAndCompare(
      PersonTable(),
      """fun newPersonRecord(firstName: String): RecordImpl {
  val result = RecordImpl()
  result.firstName = firstName
  return result
}
      """
    )
  }

  @Test
  fun `should generate with a nullable string argument`() {

    @Suppress("unused")
    class PersonTable : TableImpl<Record>(DSL.name("PERSON")) {
      val firstName = this.createField("FIRST_NAME", VARCHAR.length(255).nullable(true))
    }

    generateNewFunAndCompare(
      PersonTable(),
      """fun newPersonRecord(firstName: String? = null): RecordImpl {
  val result = RecordImpl()
  if (firstName != null) result.firstName = firstName
  return result
}
      """
    )
  }

  @Test
  fun `should generate new fun with automatic UUIDs`() {

    @Suppress("unused")
    class PersonTable : TableImpl<Record>(DSL.name("PERSON")) {
      val id = this.createField("ID", UUID.nullable(false))

      override fun getPrimaryKey() = createKey(this, id)
    }

    generateNewFunAndCompare(
      PersonTable(),
      """fun newPersonRecord(id: UUID = UUID.randomUUID()): RecordImpl {
  val result = RecordImpl()
  result.id = id
  return result
}
      """
    )
  }

  @Test
  fun `should not make fields with default values mandatory`() {

    @Suppress("unused")
    class PersonTable : TableImpl<Record>(DSL.name("PERSON")) {
      val id = this.createField("FIRST_NAME", VARCHAR.nullable(false).defaultValue("Toto"))
    }

    generateNewFunAndCompare(
      PersonTable(),
      """fun newPersonRecord(firstName: String? = null): RecordImpl {
  val result = RecordImpl()
  if (firstName != null) result.firstName = firstName
  return result
}
      """
    )
  }

  class OtherTable : TableImpl<Record>(DSL.name("Other")) {
    val id = this.createField("ID", UUID.nullable(false))
    override fun getPrimaryKey() = createKey(this, id)
  }

  @Test
  fun `should not generate provide defaut values for UUIDs in FK`() {
    val otherTable = OtherTable()

    @Suppress("unused")
    class PersonTable : TableImpl<Record>(DSL.name("PERSON")) {
      val id = this.createField("ID", UUID.nullable(false))
      val otherId = this.createField("OTHER_ID", UUID.nullable(false))

      override fun getPrimaryKey() = createKey(this, id)
      override fun getReferences() = listOf(createFk(otherTable.primaryKey, this, otherId))
    }

    generateNewFunAndCompare(
      PersonTable(),
      """fun newPersonRecord(id: UUID = UUID.randomUUID(), otherId: UUID): RecordImpl {
  val result = RecordImpl()
  result.id = id
  result.otherId = otherId
  return result
}
      """
    )
  }

  @Test
  fun `should generate update fun with keys as non-nullable args`() {

    @Suppress("unused")
    class PersonTable : TableImpl<Record>(DSL.name("PERSON")) {
      val id = this.createField("ID", VARCHAR.length(36).nullable(false))
      val firstName = this.createField("FIRST_NAME", VARCHAR.length(120).nullable(false))

      override fun getPrimaryKey() = createKey(this, id)
    }

    generateUpdateFunAndCompare(
      PersonTable(),
      """fun updatePersonRecord(id: String, firstName: String? = null): RecordImpl {
  val result = RecordImpl()
  result.id = id
  if (firstName != null) result.firstName = firstName
  result.table.primaryKey.fields.forEach { result.changed(it, false) }
  return result
}
      """
    )
  }
}

fun generateUpdateFunAndCompare(table: TableImpl<*>, @Language("kotlin") expectedCode: String) {
  val code = generateUpdateFunctionCode(table)

  assertEquals(code, expectedCode.trimIndent())
}

private fun generateUpdateFunctionCode(table: TableImpl<*>): String {
  val builder = FileSpec.builder(packageName = "", fileName = "testFile")
  addUpdateFunction(table, builder)
  return builder.build().toString().trimImports()
}

fun generateNewFunAndCompare(table: TableImpl<*>, @Language("kotlin") expectedCode: String) {
  val code = generateNewFunctionCode(table)
  assertEquals(code, expectedCode.trimIndent())
}

private fun generateNewFunctionCode(table: TableImpl<*>): String {
  val builder = FileSpec.builder(packageName = "", fileName = "testFile")
  addNewFunction(table, builder)
  return builder.build().toString().trimImports()
}

fun String.trimImports(): String {
  return Regex("import .*").replace(this, "").trim()
}
