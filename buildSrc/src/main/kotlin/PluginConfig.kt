import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

data class PluginConfig(
  val url: String,
  val user: String,
  val password: String,
  val driver: String,
  val schemas: List<String>,
  val jooqSchemas: String
)

fun loadProperties(f: File): PluginConfig {
  val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
  return moshi.adapter(PluginConfig::class.java)
    .lenient()
    .fromJson(f.readText())!!
}