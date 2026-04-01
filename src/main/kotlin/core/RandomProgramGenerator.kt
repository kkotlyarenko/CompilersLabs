package core

import kotlin.random.Random

class RandomProgramGenerator {
    private val random = Random

    // Пул имен для переменных
    private val varNames = arrayOf(
        "x", "y", "z", "alpha", "beta", "count", "total", "index", "sum"
    )

    // Уже объявленные переменные
    private val declaredVars = mutableListOf<String>()

    private val mathOps = arrayOf("+", "-", "*", "/")
    private val compareOps = arrayOf("==", "!=", "<", ">", "<=", ">=")
    private val logicOps = arrayOf("&&", "||")

    /**
     * Генерирует случайную программу
     */
    fun generate(statementCount: Int = 10): String {

        declaredVars.clear()
        val builder = StringBuilder()

        // Сразу объявляем несколько переменных
        repeat(3) {
            builder.appendLine(generateVarDeclaration(0))
        }

        generateBlock(builder, statementCount, 0)

        return builder.toString()
    }

    private fun generateBlock(builder: StringBuilder, count: Int, indentLevel: Int) {

        val indent = " ".repeat(indentLevel * 4)

        repeat(count) {

            // 0: var
            // 1: assignment
            // 2: print
            // 3: if
            // 4: while
            var statementType = random.nextInt(5)

            // Ограничиваем вложенность
            if (indentLevel > 2 && statementType > 2)
                statementType = random.nextInt(3)

            when (statementType) {

                0 -> builder.appendLine(generateVarDeclaration(indentLevel))

                1 -> {
                    if (declaredVars.isNotEmpty())
                        builder.appendLine("$indent${getRandomVar()} = ${generateExpression()};")
                    else
                        builder.appendLine(generateVarDeclaration(indentLevel))
                }

                2 -> builder.appendLine("$indent" + "print ${generateExpression()};")

                3 -> {
                    builder.appendLine("$indent" + "if (${generateCondition()}) {")
                    generateBlock(builder, random.nextInt(1, 4), indentLevel + 1)

                    if (random.nextDouble() > 0.5) {
                        builder.appendLine("$indent} else {")
                        generateBlock(builder, random.nextInt(1, 3), indentLevel + 1)
                    }

                    builder.appendLine("$indent}")
                }

                4 -> {
                    builder.appendLine("$indent" + "while (${generateCondition()}) {")
                    generateBlock(builder, random.nextInt(1, 4), indentLevel + 1)
                    builder.appendLine("$indent}")
                }
            }
        }
    }

    private fun generateVarDeclaration(indentLevel: Int): String {

        val indent = " ".repeat(indentLevel * 4)

        val varName = varNames[random.nextInt(varNames.size)]

        if (!declaredVars.contains(varName))
            declaredVars.add(varName)

        return "$indent" + "var $varName: Number = ${generateExpression()};"
    }

    private fun generateExpression(): String {

        if (random.nextDouble() > 0.6 || declaredVars.isEmpty())
            return random.nextInt(1, 100).toString()

        if (random.nextDouble() > 0.5)
            return getRandomVar()

        val left =
            if (random.nextDouble() > 0.5) getRandomVar()
            else random.nextInt(1, 100).toString()

        val right =
            if (random.nextDouble() > 0.5) getRandomVar()
            else random.nextInt(1, 100).toString()

        val op = mathOps[random.nextInt(mathOps.size)]

        return "$left $op $right"
    }

    private fun generateCondition(): String {

        val left = getRandomVarOrNumber()
        val right = getRandomVarOrNumber()
        val compOp = compareOps[random.nextInt(compareOps.size)]

        var condition = "$left $compOp $right"

        if (random.nextDouble() > 0.7) {

            val logicOp = logicOps[random.nextInt(logicOps.size)]
            val extraLeft = getRandomVarOrNumber()
            val extraRight = getRandomVarOrNumber()
            val extraComp = compareOps[random.nextInt(compareOps.size)]

            condition = "($condition) $logicOp ($extraLeft $extraComp $extraRight)"
        }

        return condition
    }

    private fun getRandomVar(): String {
        if (declaredVars.isEmpty()) return "1"
        return declaredVars[random.nextInt(declaredVars.size)]
    }

    private fun getRandomVarOrNumber(): String {
        if (declaredVars.isNotEmpty() && random.nextDouble() > 0.5)
            return getRandomVar()
        return random.nextInt(1, 100).toString()
    }
}