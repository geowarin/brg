data class PluginConfig(
        val url: String,
        val user: String,
        val password: String,
        val driver: String,
        val schemas: List<String>
)