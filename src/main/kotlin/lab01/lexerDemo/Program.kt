import core.RandomProgramGenerator
import core.lexer.Lexer

object Program {
    @JvmStatic
    fun main(args: Array<String>) {
        val generator = RandomProgramGenerator()

        val randomCode = generator.generate(10)

        println("=== СГЕНЕРИРОВАННЫЙ КОД ===")
        println(randomCode)
        println("===========================\n")

        val lexer = Lexer(randomCode)
        val tokens = lexer.tokenize()

        println("=== ТОКЕНЫ ===")
        for (token in tokens) {
            println(token)
        }

        readlnOrNull()
    }
}