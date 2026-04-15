package org.kkotlyarenko.lab05.interpreterDemo

import org.kkotlyarenko.core.interpreter.Interpreter
import org.kkotlyarenko.core.lexer.Lexer
import org.kkotlyarenko.core.parser.Parser

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