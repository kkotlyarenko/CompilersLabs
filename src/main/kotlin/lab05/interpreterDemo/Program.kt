package lab05.interpreterDemo

import core.interpreter.Interpreter
import core.lexer.Lexer
import core.parser.Parser

object Program {
    private val arithmeticDemoCode = """
        var x = 10;
        var y = 4;

        print x + y;
        print x - y;
        print x * y;
        print x / y;

        var total = 0;
        var i = 1;
        while (i <= 5) {
            total = total + i;
            i = i + 1;
        }

        print total;

        if ((total > 10) && (x != y)) {
            print "arithmetics work";
        } else {
            print "unexpected result";
        }
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        runInterpreter(arithmeticDemoCode)
    }

    private fun runInterpreter(sourceCode: String) {
        println("=== CODE ===")
        println(sourceCode)

        try {
            val lexer = Lexer(sourceCode)
            val tokens = lexer.tokenize()

            val parser = Parser(tokens)
            val ast = parser.parse()

            println("\nSuccessfully parsed: ${ast.size} instructions.")
            println("=== INTERPRETER OUTPUT ===")

            val interpreter = Interpreter()
            interpreter.interpret(ast)
        } catch (ex: Exception) {
            println("Error running program: ${ex.message}")
        }
    }
}