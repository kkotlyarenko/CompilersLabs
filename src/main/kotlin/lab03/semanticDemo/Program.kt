package lab03.semanticDemo

import core.RandomProgramGenerator
import core.lexer.Lexer
import core.semantic.SemanticAnalyzer
import core.parser.Parser

object Program {
    private val validSampleCode = """
        var x: Number = 1;
        var y: Number = 2;
        var label: String = "sum:";
        var enabled: Boolean = true;

        if (enabled && (x < y)) {
            print label + " ok";
            print x + y;
        } else {
            print "disabled";
        }

        var i: Number = 0;
        while (i < 3) {
            print i;
            i = i + 1;
        }
    """.trimIndent()

    private val typeMismatchCode = """
        var score: Number = "oops";
        var flag: Boolean = 42;
        var name: String = true;
    """.trimIndent()

    private val missingAnnotationCode = """
        var x = 10;
        var msg = "hello";
        var flag = true;
    """.trimIndent()

    private val uninitializedCode = """
        var counter: Number;
        print counter;
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        val generator = RandomProgramGenerator()

        println("=== VALID EXAMPLE ===")
        runSemanticAnalysis(validSampleCode)
        println()

        println("=== TYPE MISMATCH ERRORS ===")
        runSemanticAnalysis(typeMismatchCode)
        println()

        println("=== MISSING TYPE ANNOTATION ===")
        runSemanticAnalysis(missingAnnotationCode)
        println()

        println("=== UNINITIALIZED VARIABLE ===")
        runSemanticAnalysis(uninitializedCode)
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
