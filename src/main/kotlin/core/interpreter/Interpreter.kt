package core.interpreter

import core.lexer.TokenType
import core.parser.ast.AssignExpression
import core.parser.ast.BinaryExpression
import core.parser.ast.BlockStatement
import core.parser.ast.BooleanExpression
import core.parser.ast.Expression
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

class Interpreter(private val output: (String) -> Unit = { println(it) }) {
    private var environment = RuntimeEnvironment()

    fun interpret(statements: Iterable<Statement>) {
        environment = RuntimeEnvironment()
        for (statement in statements) {
            execute(statement)
        }
    }

    private fun execute(statement: Statement) {
        when (statement) {
            is ExpressionStatement -> evaluate(statement.expression)
            is PrintStatement -> output(stringify(evaluate(statement.expression)))
            is VarStatement -> {
                val value = statement.initializer?.let { evaluate(it) }
                environment.define(statement.name, value)
            }

            is BlockStatement -> executeBlock(statement.statements, RuntimeEnvironment(environment))

            is IfStatement -> {
                if (asBoolean(evaluate(statement.condition), "If condition must be Boolean.")) {
                    execute(statement.thenBranch)
                } else {
                    statement.elseBranch?.let { execute(it) }
                }
            }

            is WhileStatement -> {
                while (asBoolean(evaluate(statement.condition), "While condition must be Boolean.")) {
                    execute(statement.body)
                }
            }

            else -> throw RuntimeException("Unsupported statement type: ${statement::class.simpleName}")
        }
    }

    private fun executeBlock(statements: Iterable<Statement>, blockEnvironment: RuntimeEnvironment) {
        val previous = environment
        environment = blockEnvironment
        try {
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            environment = previous
        }
    }

    private fun evaluate(expression: Expression): Any? {
        return when (expression) {
            is NumberExpression -> expression.value
            is StringExpression -> expression.value
            is BooleanExpression -> expression.value

            is VariableExpression -> environment.get(expression.name)

            is AssignExpression -> {
                val value = evaluate(expression.value)
                environment.assign(expression.name, value)
                value
            }

            is UnaryExpression -> {
                val right = evaluate(expression.right)
                when (expression.operator) {
                    TokenType.MINUS -> -asNumber(right, "Unary '-' requires Number operand.")
                    TokenType.EXCL -> !asBoolean(right, "Unary '!' requires Boolean operand.")
                    else -> throw RuntimeException("Unsupported unary operator '${expression.operator}'.")
                }
            }

            is BinaryExpression -> evaluateBinary(expression)

            else -> throw RuntimeException("Unsupported expression type: ${expression::class.simpleName}")
        }
    }

    private fun evaluateBinary(expression: BinaryExpression): Any {
        if (expression.operator == TokenType.AND) {
            val left = asBoolean(evaluate(expression.left), "Operator '&&' requires Boolean operands.")
            return if (!left) false else asBoolean(
                evaluate(expression.right),
                "Operator '&&' requires Boolean operands."
            )
        }

        if (expression.operator == TokenType.OR) {
            val left = asBoolean(evaluate(expression.left), "Operator '||' requires Boolean operands.")
            return if (left) true else asBoolean(
                evaluate(expression.right),
                "Operator '||' requires Boolean operands."
            )
        }

        val left = evaluate(expression.left)
        val right = evaluate(expression.right)

        return when (expression.operator) {
            TokenType.PLUS -> {
                when {
                    left is Double && right is Double -> left + right
                    left is String && right is String -> left + right
                    else -> throw RuntimeException("Operator '+' supports Number+Number or String+String.")
                }
            }

            TokenType.MINUS -> asNumber(left, "Operator '-' requires Number operands.") -
                asNumber(right, "Operator '-' requires Number operands.")

            TokenType.STAR -> asNumber(left, "Operator '*' requires Number operands.") *
                asNumber(right, "Operator '*' requires Number operands.")

            TokenType.SLASH -> {
                val denominator = asNumber(right, "Operator '/' requires Number operands.")
                if (denominator == 0.0) {
                    throw RuntimeException("Division by zero.")
                }

                asNumber(left, "Operator '/' requires Number operands.") / denominator
            }

            TokenType.LT -> asNumber(left, "Operator '<' requires Number operands.") <
                asNumber(right, "Operator '<' requires Number operands.")

            TokenType.LTEQ -> asNumber(left, "Operator '<=' requires Number operands.") <=
                asNumber(right, "Operator '<=' requires Number operands.")

            TokenType.GT -> asNumber(left, "Operator '>' requires Number operands.") >
                asNumber(right, "Operator '>' requires Number operands.")

            TokenType.GTEQ -> asNumber(left, "Operator '>=' requires Number operands.") >=
                asNumber(right, "Operator '>=' requires Number operands.")

            TokenType.EQEQ -> left == right
            TokenType.NEQ -> left != right

            else -> throw RuntimeException("Unsupported binary operator '${expression.operator}'.")
        }
    }

    private fun asNumber(value: Any?, message: String): Double {
        return value as? Double ?: throw RuntimeException(message)
    }

    private fun asBoolean(value: Any?, message: String): Boolean {
        return value as? Boolean ?: throw RuntimeException(message)
    }

    private fun stringify(value: Any?): String {
        if (value == null) {
            return "nil"
        }

        if (value is Double && value % 1.0 == 0.0) {
            return value.toLong().toString()
        }

        return value.toString()
    }
}
