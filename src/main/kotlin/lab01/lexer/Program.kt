package org.kkotlyarenko.lab01.lexer

import org.kkotlyarenko.core.lexer.Lexer
import kotlin.random.Random

object Program {
    @JvmStatic
    fun main(args: Array<String>) {
        val codeExample = "var x = 123; print x + 5;"

        val lexer = Lexer(codeExample)
        val tokens = lexer.tokenize()

        for (token in tokens) {
            println(token)
        }

        repeat(3) {
            println("\n--- Generating random test program ---\n")
            val randomProgram = generateRandomTestProgram()
            println(randomProgram)

            val randomLexer = Lexer(randomProgram)
            val randomTokens = randomLexer.tokenize()

            for (token in randomTokens) {
                println(token)
            }
        }

        readlnOrNull()
    }

    private fun generateRandomTestProgram(): String {
        val random = Random.Default
        val variables = arrayOf("a", "b", "c", "x", "y", "z")
        val operators = arrayOf("+", "-", "*", "/")

        val program = StringBuilder()

        repeat(5) {
            val varName = variables[random.nextInt(variables.size)]
            val number = random.nextInt(1, 100)
            program.appendLine("var $varName = $number;")
        }

        repeat(5) {
            val var1 = variables[random.nextInt(variables.size)]
            val var2 = variables[random.nextInt(variables.size)]
            val op = operators[random.nextInt(operators.size)]
            program.appendLine("print $var1 $op $var2;")
        }

        return program.toString()
    }
}
