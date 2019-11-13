package codegen

import com.squareup.kotlinpoet.FileSpec
import org.intellij.lang.annotations.Language
import org.jooq.Record
import org.jooq.impl.DSL.name
import org.jooq.impl.SQLDataType.UUID
import org.jooq.impl.SQLDataType.VARCHAR
import org.jooq.impl.TableImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinTableFunctionsGeneratorKtTest {

  @Test
  fun `should generate new record function with a non-nullable string argument`() {

    class PersonTable : TableImpl<Record>(name("PERSON")) {
      init {
        createField(name("FIRST_NAME"), VARCHAR.nullable(false))
      }
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

    class PersonTable : TableImpl<Record>(name("PERSON")) {
      init {
        createField(name("FIRST_NAME"), VARCHAR.nullable(true))
      }
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

    class PersonTable : TableImpl<Record>(name("PERSON")) {
      val id = createField(name("ID"), UUID.nullable(false))

      override fun getPrimaryKey() = createKey(id)
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

    class PersonTable : TableImpl<Record>(name("PERSON")) {
      init {
        createField(name("FIRST_NAME"), VARCHAR.nullable(false).defaultValue("Toto"))
      }
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

  class OtherTable : TableImpl<Record>(name("Other")) {
    private val id = createField(name("ID"), UUID.nullable(false))
    override fun getPrimaryKey() = createKey(id)
  }

  @Test
  fun `should not provide default values for UUIDs in FK`() {
    val otherTable = OtherTable()

    class PersonTable : TableImpl<Record>(name("PERSON")) {
      val id = createField(name("ID"), UUID.nullable(false))
      val otherId = createField(name("OTHER_ID"), UUID.nullable(false))

      override fun getPrimaryKey() = createKey(id)
      override fun getReferences() = createFks(otherTable.primaryKey to otherId)
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

    class PersonTable : TableImpl<Record>(name("PERSON")) {
      val id = createField(name("ID"), VARCHAR.nullable(false))

      init {
        createField(name("FIRST_NAME"), VARCHAR.nullable(false))
      }

      override fun getPrimaryKey() = createKey(id)
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
