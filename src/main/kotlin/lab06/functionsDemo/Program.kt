package lab06.functionsDemo

import core.interpreter.Interpreter
import core.lexer.Lexer
import org.kkotlyarenko.core.parser.Parser

object Program {
    private val functionsDemoCode = """
        fun add(a, b) {
            return a + b;
        }
        
        fun addAndSquare(a, b) {
            return add(a, b) * add(a, b);
       }
        
        fun factorial(n) {
            if (n <= 1) {
                return 1;
            }
            return n * factorial(n - 1);
        }

        fun makeAdder(x) {
            fun addTo(y) {
                return x + y;
            }
            return addTo;
        }

        var result = add(3, 4);
        print result;

        print factorial(5);

        var plusTen = makeAdder(10);
        print plusTen(7);
        
        print addAndSquare(1, 2);
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        runInterpreter(functionsDemoCode)
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
