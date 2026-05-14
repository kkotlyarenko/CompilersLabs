package lab07.optimizationDemo

import core.interpreter.Interpreter
import core.lexer.Lexer
import core.optimizer.ConstantFoldingOptimizer
import lab02.parserDemo.AstPrinter
import core.parser.Parser

object Program {
    private val demoCode = """
        var numberPart = (1 + 2) * (3 + 4);
        var textPart = "Hello, " + "world";
        var mixed = textPart + "!";

        print numberPart;
        print textPart;
        print mixed;
        print (10 - 4) == 6;

        if ((true && true) || false) {
            print "branch stays the same";
        } else {
            print "unexpected";
        }
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        runOptimizationDemo(demoCode)
    }

    private fun runOptimizationDemo(sourceCode: String) {
        println("=== CODE ===")
        println(sourceCode)

        try {
            val lexer = Lexer(sourceCode)
            val tokens = lexer.tokenize()

            val parser = Parser(tokens)
            val ast = parser.parse()

            println("\n=== ORIGINAL AST ===")
            AstPrinter().print(ast)

            val optimizer = ConstantFoldingOptimizer()
            val optimizedAst = optimizer.optimize(ast)

            println("\n=== OPTIMIZED AST ===")
            AstPrinter().print(optimizedAst)

            val originalOutput = runInterpreter(ast)
            val optimizedOutput = runInterpreter(optimizedAst)

            println("\n=== ORIGINAL OUTPUT ===")
            originalOutput.forEach { println(it) }

            println("\n=== OPTIMIZED OUTPUT ===")
            optimizedOutput.forEach { println(it) }

            println(
                "\nSemantics preserved: ${if (originalOutput == optimizedOutput) "yes" else "no"}"
            )
        } catch (ex: Exception) {
            println("Error running optimization demo: ${ex.message}")
        }
    }

    private fun runInterpreter(statements: Iterable<core.parser.ast.Statement>): List<String> {
        val output = mutableListOf<String>()
        val interpreter = Interpreter { output.add(it) }
        interpreter.interpret(statements)
        return output
    }
}