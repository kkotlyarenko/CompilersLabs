package lab02.parserDemo

import core.RandomProgramGenerator
import core.lexer.Lexer
import core.parser.Parser

object Program {
    @JvmStatic
    fun main(args: Array<String>) {
        val generator = RandomProgramGenerator()

        val randomCode = generator.generate(10)
        println(randomCode)

        val lexer = Lexer(randomCode)
        val tokens = lexer.tokenize()

        val parser = Parser(tokens)
        val ast = parser.parse()

        println("Успешно распарсено: ${ast.size} инструкций на верхнем уровне.")

        val printer = AstPrinter()
        printer.print(ast)

        readlnOrNull()
    }
}
