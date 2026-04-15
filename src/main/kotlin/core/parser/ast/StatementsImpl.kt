package core.parser.ast

class ExpressionStatement(val expression: Expression) : Statement()

class PrintStatement(val expression: Expression) : Statement()

class VarStatement(
    val name: String,
    val declaredType: String?,
    val initializer: Expression?
) : Statement()

class BlockStatement(val statements: MutableList<Statement>) : Statement()

class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement? = null
) : Statement()

class WhileStatement(
    val condition: Expression,
    val body: Statement
) : Statement()