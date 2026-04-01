package core.lexer


class Token(
    val type: TokenType,
    val value: String,
    val position: Int,
    val line: Int,
    val column: Int
) {
    override fun toString(): String {
        return "[$line:$column] Token(type=$type, value='$value', position=$position)"
    }
}