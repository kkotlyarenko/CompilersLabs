package lab02.parserDemo

import core.parser.ast.AssignExpression
import core.parser.ast.BinaryExpression
import core.parser.ast.BlockStatement
import core.parser.ast.BooleanExpression
import core.parser.ast.ExpressionStatement
import core.parser.ast.IfStatement
import core.parser.ast.NumberExpression
import core.parser.ast.PrintStatement
import core.parser.ast.Statement
import core.parser.ast.StringExpression
import core.parser.ast.UnaryExpression
import core.parser.ast.VarStatement
import core.parser.ast.VariableExpression
import core.parser.ast.WhileStatement


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
        print(indent + marker)

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
            is StringExpression -> println("String: \"${node.value}\"")
            is BooleanExpression -> println("Boolean: ${node.value}")
            is VariableExpression -> println("Variable: ${node.name}")
            else -> println("Unknown Node: ${node::class.simpleName}")
        }
    }
}
