package org.kkotlyarenko.lab02.parserDemo

import org.kkotlyarenko.core.parser.ast.*


class AstPrinter {
    fun print(statements: List<Statement>) {
        println("Root (Program)")
        for (i in statements.indices) {
            printNode(statements[i], "", i == statements.lastIndex)
        }
    }

    private fun printNode(node: Any?, indent: String, isLast: Boolean) {
        if (node == null) return

        val marker = if (isLast) "└── " else "├── "
        kotlin.io.print(indent + marker)

        val childIndent = indent + if (isLast) "    " else "│   "

        when (node) {
            is VarStatement -> {
                println("VarStatement: ${node.name}")
                if (node.initializer != null) {
                    printNode(node.initializer, childIndent, true)
                }
            }

            is PrintStatement -> {
                println("PrintStatement")
                printNode(node.expression, childIndent, true)
            }

            is IfStatement -> {
                println("IfStatement")
                printNode(node.condition, childIndent, false)
                printNode(node.thenBranch, childIndent, node.elseBranch == null)

                if (node.elseBranch != null) {
                    printNode(node.elseBranch, childIndent, true)
                }
            }

            is WhileStatement -> {
                println("WhileStatement")
                printNode(node.condition, childIndent, false)
                printNode(node.body, childIndent, true)
            }

            is BlockStatement -> {
                println("BlockStatement")
                for (j in node.statements.indices) {
                    printNode(node.statements[j], childIndent, j == node.statements.lastIndex)
                }
            }

            is ExpressionStatement -> {
                println("ExpressionStatement")
                printNode(node.expression, childIndent, true)
            }

            is BinaryExpression -> {
                println("BinaryExpression: ${node.operator}")
                printNode(node.left, childIndent, false)
                printNode(node.right, childIndent, true)
            }

            is UnaryExpression -> {
                println("UnaryExpression: ${node.operator}")
                printNode(node.right, childIndent, true)
            }

            is AssignExpression -> {
                println("AssignExpression: ${node.name} =")
                printNode(node.value, childIndent, true)
            }

            is NumberExpression -> println("Number: ${node.value}")
            is VariableExpression -> println("Variable: ${node.name}")
            else -> println("Unknown Node: ${node::class.simpleName}")
        }
    }
}
