package codegen

import com.google.common.base.CaseFormat
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import org.jooq.Catalog
import org.jooq.Field
import org.jooq.Schema
import org.jooq.Table
import java.io.File
import java.util.*
import kotlin.reflect.full.isSubclassOf


const val prefixPackage = "com.geowarin.model.builders."

fun main(args: Array<String>) {

  val destination = args[0]
  val catalogClassName = args[1]

  val catalogClass = Class.forName(catalogClassName)
  executeCodegen(File(destination), catalogClass)
}

fun executeCodegen(destination: File, catalogClass: Class<*>) {

  schemas(catalogClass).forEach { schema ->

    val packageName = prefixPackage + camelize(schema.name)
    val builder = FileSpec.builder(packageName, "tableBuilders")

    schema.tables.forEach { table ->
      addNewFunction(table, builder)
      addUpdateFunction(table, builder)
    }

    val kotlinFile = builder.build()

    destination.mkdirs()
    kotlinFile.writeTo(destination)
  }
}

private fun schemas(catalogClass: Class<*>): List<Schema> {
  val field = catalogClass.getDeclaredField("DEFAULT_CATALOG")
  val catalogInst = field.get(catalogClass) as Catalog
  return catalogInst.schemas
}

fun addNewFunction(table: Table<*>, builder: FileSpec.Builder) {
  val funSpec = FunSpec.builder(camelize("NEW_" + table.name + "_RECORD"))
  fun isUUID(field: Field<*>) = field.type.kotlin.isSubclassOf(UUID::class)
  fun isFK(field: Field<*>) = table.references.any { it.fields.contains(field) }
  fun hasDefaultValue(field: Field<*>) = field.dataType.defaulted()
  fun isNullable(field: Field<*>) = field.dataType.nullable()
  fun isOptional(field: Field<*>) = isNullable(field) || hasDefaultValue(field)

  table.fields().forEach { field ->
    val type = ClassName.bestGuess(field.type.kotlin.qualifiedName!!)

    val className = type.copy(nullable = isOptional(field))
    val paramBuilder = ParameterSpec.builder(camelize(field.name), className)

    if (isOptional(field)) {
      paramBuilder.defaultValue("null")
    } else if (isUUID(field) && !isFK(field)) {
      paramBuilder.defaultValue("%T.randomUUID()", UUID::class)
    }

    funSpec.addParameter(paramBuilder.build())
    funSpec.returns(table.recordType)
  }

  funSpec.addStatement("val result = %T()", table.recordType)
  table.fields().forEach { field ->
    addInitFieldStatement(field, funSpec, ::isOptional)
  }
  funSpec.addStatement("return result")

  builder.addFunction(funSpec.build())
}

private fun addInitFieldStatement(field: Field<*>, funSpec: FunSpec.Builder, isOptional: (Field<*>) -> Boolean) {
  val camelCasedName = camelize(field.name)
  if (isOptional(field)) {
    funSpec.addStatement("if (%L != null) result.%L = %L", camelCasedName, camelCasedName, camelCasedName)
  } else {
    funSpec.addStatement("result.%L = %L", camelCasedName, camelCasedName)
  }
}

fun addUpdateFunction(table: Table<*>, builder: FileSpec.Builder) {
  val funSpec = FunSpec.builder(camelize("UPDATE_" + table.name + "_RECORD"))
  fun isKey(field: Field<*>) = table.primaryKey?.fields?.contains(field) ?: false
  fun isOptional(field: Field<*>) = !isKey(field)

  table.fields().forEach { field ->

    val type = ClassName.bestGuess(field.type.kotlin.qualifiedName!!)
    val className = type.copy(nullable = !isKey(field))
    val paramBuilder = ParameterSpec.builder(camelize(field.name), className)

    if (isOptional(field)) {
      paramBuilder.defaultValue("null")
    }

    funSpec.addParameter(paramBuilder.build())
    funSpec.returns(table.recordType)
  }

  funSpec.addStatement("val result = %T()", table.recordType)
  table.fields().forEach { field ->
    addInitFieldStatement(field, funSpec, ::isOptional)
  }

  funSpec.addStatement("result.table.primaryKey.fields.forEach { result.changed(it, false) }")
  funSpec.addStatement("return result")

  builder.addFunction(funSpec.build())
}

fun camelize(str: String): String {
  return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str)
}
