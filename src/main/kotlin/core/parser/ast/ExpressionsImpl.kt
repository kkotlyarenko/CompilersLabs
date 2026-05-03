package core.parser.ast

import core.lexer.TokenType

class NumberExpression(val value: Double) : Expression()

class StringExpression(val value: String) : Expression()

class BooleanExpression(val value: Boolean) : Expression()

class VariableExpression(val name: String) : Expression()

class BinaryExpression(
    val left: Expression,
    val operator: TokenType,
    val right: Expression
) : Expression()

class UnaryExpression(
    val operator: TokenType,
    val right: Expression
) : Expression()

class AssignExpression(
    val name: String,
    val value: Expression
) : Expression()

class CallExpression(
    val callee: Expression,
    val arguments: List<Expression>
) : Expression()
