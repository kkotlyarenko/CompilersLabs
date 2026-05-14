package lab03.semanticDemo

import core.RandomProgramGenerator
import core.lexer.Lexer
import core.parser.Parser
import core.semantic.SemanticAnalyzer

object Program {
    private val validSampleCode = """
        var x = 1;
        var y = 2;
        var p = 1;

        if (x < y) {
            print x + y;
        } else {
            print y - x;
        }

        var i = 0;
        while (i < 3) {
            print i;
            i = i + 1;
        }
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        val generator = RandomProgramGenerator()

        println("=== VALID EXAMPLE ===")
        runSemanticAnalysis(validSampleCode)
        println()

        while (true) {
            println("Enter to continue, q to quit")
            val input = readlnOrNull()
            if (input.equals("q", ignoreCase = true)) {
                break
            }

            val randomCode = generator.generate(10)
            println("\n=== RANDOM CODE ===")
            runSemanticAnalysis(randomCode)

            println()
        }
    }

    private fun runSemanticAnalysis(sourceCode: String) {
        println("\n=== CODE ===")
        println(sourceCode)

        try {
            val lexer = Lexer(sourceCode)
            val tokens = lexer.tokenize()

            val parser = Parser(tokens)
            val ast = parser.parse()

            println("Successfully parsed: ${ast.size} instructions.")

            val semanticAnalyzer = SemanticAnalyzer()
            semanticAnalyzer.analyze(ast)

            if (semanticAnalyzer.errors.isNotEmpty()) {
                println("Semantic analyze finished with errors:")
                for (error in semanticAnalyzer.errors) {
                    println("- $error")
                }
            } else {
                println("Semantic errors not found.")
            }

            if (semanticAnalyzer.warnings.isNotEmpty()) {
                println("Warnings:")
                for (warning in semanticAnalyzer.warnings) {
                    println("- $warning")
                }
            } else {
                println("No warnings.")
            }
        } catch (ex: Exception) {
            println("Error running program: ${ex.message}")
        }
    }
}
