package codegen

import com.geowarin.jooq.createFks
import com.geowarin.jooq.createKey
import com.geowarin.jooq.table
import com.squareup.kotlinpoet.FileSpec
import org.intellij.lang.annotations.Language
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.name
import org.jooq.impl.SQLDataType.UUID
import org.jooq.impl.SQLDataType.VARCHAR
import org.jooq.impl.TableImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinTableFunctionsGeneratorKtTest {

  @Test
  fun `should generate new record function with a non-nullable string argument`() {
    val personTable = table {
      field("FIRST_NAME", VARCHAR.nullable(false))
    }

    generateNewFunAndCompare(
      personTable,
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

    val personTable = table {
      field("FIRST_NAME", VARCHAR.nullable(true))
    }

    generateNewFunAndCompare(
      personTable,
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

    val personTable = table {
      pk("ID", UUID.nullable(false))
    }

    generateNewFunAndCompare(
      personTable,
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
    val personTable = table {
      field("FIRST_NAME", VARCHAR.nullable(false).defaultValue("Toto"))
    }

    generateNewFunAndCompare(
      personTable,
      """fun newPersonRecord(firstName: String? = null): RecordImpl {
  val result = RecordImpl()
  if (firstName != null) result.firstName = firstName
  return result
}
      """
    )
  }

  private val otherTable = table {
    pk("ID", UUID.nullable(false))
  }

  @Test
  fun `should not provide default values for UUIDs in FK`() {

    val personTable = table {
      pk("ID", UUID.nullable(false))
      field("OTHER_ID", UUID.nullable(false)) fkOn otherTable
    }

    generateNewFunAndCompare(
      personTable,
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

    val personTable = table {
      pk("ID", VARCHAR.nullable(false))
      field("FIRST_NAME", VARCHAR.nullable(false))
    }

    generateUpdateFunAndCompare(
      personTable,
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

fun generateUpdateFunAndCompare(table: Table<*>, @Language("kotlin") expectedCode: String) {
  val code = generateUpdateFunctionCode(table)

  assertEquals(code, expectedCode.trimIndent())
}

private fun generateUpdateFunctionCode(table: Table<*>): String {
  val builder = FileSpec.builder(packageName = "", fileName = "testFile")
  addUpdateFunction(table, builder)
  return builder.build().toString().trimImports()
}

fun generateNewFunAndCompare(table: Table<*>, @Language("kotlin") expectedCode: String) {
  val code = generateNewFunctionCode(table)
  assertEquals(code, expectedCode.trimIndent())
}

private fun generateNewFunctionCode(table: Table<*>): String {
  val builder = FileSpec.builder(packageName = "", fileName = "testFile")
  addNewFunction(table, builder)
  return builder.build().toString().trimImports()
}

fun String.trimImports(): String {
  return Regex("import .*").replace(this, "").trim()
}
