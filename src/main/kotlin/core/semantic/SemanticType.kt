package core.semantic

enum class SemanticType {
    NUMBER,
    STRING,
    BOOLEAN;

    override fun toString(): String {
        return when (this) {
            NUMBER -> "Number"
            STRING -> "String"
            BOOLEAN -> "Boolean"
        }
    }

    companion object {
        fun fromName(name: String): SemanticType? {
            return when (name) {
                "Number" -> NUMBER
                "String" -> STRING
                "Boolean" -> BOOLEAN
                else -> null
            }
        }
    }
}
