package uchat.misc

object Utils {
    fun splitJsons(string: String): List<String> {
        return string.split("\n").filter { it.isNotBlank() || it.isNotEmpty() }.toList()
    }
}