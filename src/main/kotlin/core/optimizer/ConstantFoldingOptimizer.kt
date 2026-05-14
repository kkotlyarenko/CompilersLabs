package core.optimizer

import core.lexer.TokenType
import core.parser.ast.AssignExpression
import core.parser.ast.BinaryExpression
import core.parser.ast.BlockStatement
import core.parser.ast.BooleanExpression
import core.parser.ast.CallExpression
import core.parser.ast.Expression
import core.parser.ast.ExpressionStatement
import core.parser.ast.FunctionStatement
import core.parser.ast.IfStatement
import core.parser.ast.NumberExpression
import core.parser.ast.PrintStatement
import core.parser.ast.ReturnStatement
import core.parser.ast.Statement
import core.parser.ast.StringExpression
import core.parser.ast.UnaryExpression
import core.parser.ast.VarStatement
import core.parser.ast.WhileStatement

class ConstantFoldingOptimizer {
    fun optimize(statements: Iterable<Statement>): MutableList<Statement> {
        return statements.mapTo(mutableListOf()) { optimizeStatement(it) }
    }

    fun optimizeStatement(statement: Statement): Statement {
        return when (statement) {
            is ExpressionStatement -> ExpressionStatement(optimizeExpression(statement.expression))
            is PrintStatement -> PrintStatement(optimizeExpression(statement.expression))
            is VarStatement -> VarStatement(
                statement.name,
                statement.declaredType,
                statement.initializer?.let { optimizeExpression(it) }
            )

            is BlockStatement -> BlockStatement(statement.statements.mapTo(mutableListOf()) { optimizeStatement(it) })

            is IfStatement -> IfStatement(
                optimizeExpression(statement.condition),
                optimizeStatement(statement.thenBranch),
                statement.elseBranch?.let { optimizeStatement(it) }
            )

            is WhileStatement -> WhileStatement(
                optimizeExpression(statement.condition),
                optimizeStatement(statement.body)
            )

            is FunctionStatement -> FunctionStatement(
                statement.name,
                statement.parameters,
                statement.body.mapTo(mutableListOf()) { optimizeStatement(it) }
            )

            is ReturnStatement -> ReturnStatement(statement.value?.let { optimizeExpression(it) })
            else -> statement
        }
    }

    fun optimizeExpression(expression: Expression): Expression {
        return when (expression) {
            is NumberExpression -> expression
            is StringExpression -> expression
            is BooleanExpression -> expression

            is AssignExpression -> AssignExpression(expression.name, optimizeExpression(expression.value))

            is CallExpression -> CallExpression(
                optimizeExpression(expression.callee),
                expression.arguments.map { optimizeExpression(it) }
            )

            is UnaryExpression -> {
                val right = optimizeExpression(expression.right)
                foldUnary(expression.operator, right) ?: UnaryExpression(expression.operator, right)
            }

            is BinaryExpression -> {
                val left = optimizeExpression(expression.left)
                val right = optimizeExpression(expression.right)
                foldBinary(expression.operator, left, right) ?: BinaryExpression(left, expression.operator, right)
            }

            else -> expression
        }
    }

    private fun foldUnary(operator: TokenType, right: Expression): Expression? {
        return when (operator) {
            TokenType.MINUS -> {
                val number = (right as? NumberExpression)?.value ?: return null
                NumberExpression(-number)
            }

            TokenType.EXCL -> {
                val boolean = (right as? BooleanExpression)?.value ?: return null
                BooleanExpression(!boolean)
            }

            else -> null
        }
    }

    private fun foldBinary(operator: TokenType, left: Expression, right: Expression): Expression? {
        return when (operator) {
            TokenType.PLUS -> foldPlus(left, right)
            TokenType.MINUS -> foldNumberBinary(left, right) { a, b -> NumberExpression(a - b) }
            TokenType.STAR -> foldNumberBinary(left, right) { a, b -> NumberExpression(a * b) }
            TokenType.SLASH -> foldDivision(left, right)
            TokenType.LT -> foldNumberBinary(left, right) { a, b -> BooleanExpression(a < b) }
            TokenType.LTEQ -> foldNumberBinary(left, right) { a, b -> BooleanExpression(a <= b) }
            TokenType.GT -> foldNumberBinary(left, right) { a, b -> BooleanExpression(a > b) }
            TokenType.GTEQ -> foldNumberBinary(left, right) { a, b -> BooleanExpression(a >= b) }
            TokenType.EQEQ -> foldEquality(left, right, equals = true)
            TokenType.NEQ -> foldEquality(left, right, equals = false)
            TokenType.AND -> foldBooleanBinary(left, right) { a, b -> BooleanExpression(a && b) }
            TokenType.OR -> foldBooleanBinary(left, right) { a, b -> BooleanExpression(a || b) }
            else -> null
        }
    }

    private fun foldPlus(left: Expression, right: Expression): Expression? {
        val leftNumber = (left as? NumberExpression)?.value
        val rightNumber = (right as? NumberExpression)?.value
        if (leftNumber != null && rightNumber != null) {
            return NumberExpression(leftNumber + rightNumber)
        }

        val leftString = (left as? StringExpression)?.value
        val rightString = (right as? StringExpression)?.value
        if (leftString != null && rightString != null) {
            return StringExpression(leftString + rightString)
        }

        return null
    }

    private fun foldNumberBinary(
        left: Expression,
        right: Expression,
        operation: (Double, Double) -> Expression
    ): Expression? {
        val leftNumber = (left as? NumberExpression)?.value ?: return null
        val rightNumber = (right as? NumberExpression)?.value ?: return null
        return operation(leftNumber, rightNumber)
    }

    private fun foldDivision(left: Expression, right: Expression): Expression? {
        val leftNumber = (left as? NumberExpression)?.value ?: return null
        val rightNumber = (right as? NumberExpression)?.value ?: return null
        if (rightNumber == 0.0) {
            return null
        }

        return NumberExpression(leftNumber / rightNumber)
    }

    private fun foldBooleanBinary(
        left: Expression,
        right: Expression,
        operation: (Boolean, Boolean) -> Expression
    ): Expression? {
        val leftBoolean = (left as? BooleanExpression)?.value ?: return null
        val rightBoolean = (right as? BooleanExpression)?.value ?: return null
        return operation(leftBoolean, rightBoolean)
    }

    private fun foldEquality(left: Expression, right: Expression, equals: Boolean): Expression? {
        val result = when {
            left is NumberExpression && right is NumberExpression -> left.value == right.value
            left is StringExpression && right is StringExpression -> left.value == right.value
            left is BooleanExpression && right is BooleanExpression -> left.value == right.value
            else -> null
        } ?: return null

        return BooleanExpression(if (equals) result else !result)
    }
}