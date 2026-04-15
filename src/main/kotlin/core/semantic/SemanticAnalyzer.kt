package core.semantic

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

class SemanticAnalyzer {
    private var environment = SemanticEnvironment()
    private val _errors = mutableListOf<String>()
    private val _warnings = mutableListOf<String>()

    fun analyze(statements: Iterable<Statement>) {
        environment = SemanticEnvironment()
        _errors.clear()
        _warnings.clear()

        for (statement in statements) {
            visitStatement(statement)
        }

        collectScopeWarnings(environment)
    }

    fun visitStatement(statement: Statement) {
        when (statement) {
            is VarStatement -> {
                val declaredType = if (statement.declaredType != null) {
                    SemanticType.Companion.fromName(statement.declaredType)
                } else {
                    null
                }

                if (statement.declaredType != null && declaredType == null) {
                    _errors.add("Unknown type '${statement.declaredType}' for variable '${statement.name}'.")
                }

                val initializerType = statement.initializer?.let { inferExpressionType(it) }

                val variableType = when {
                    declaredType != null -> declaredType
                    initializerType != null -> initializerType
                    else -> {
                        _errors.add(
                            "Variable '${statement.name}' must have a type annotation or initializer for static typing."
                        )
                        null
                    }
                }

                if (declaredType != null && initializerType != null && declaredType != initializerType) {
                    _errors.add(
                        "Type mismatch in variable '${statement.name}': expected $declaredType, got $initializerType."
                    )
                }

                val isDefinedInOuterScope = environment.isVariableDefinedInOuterScopes(statement.name)

                if (variableType == null) {
                    return
                }

                if (!environment.defineVariable(statement.name, variableType, initializerType != null)) {
                    _errors.add("Variable '${statement.name}' is already defined.")
                } else {
                    if (isDefinedInOuterScope) {
                        _warnings.add("Variable '${statement.name}' shadows a variable from an outer scope.")
                    }
                }
            }

            is PrintStatement -> inferExpressionType(statement.expression)
            is ExpressionStatement -> inferExpressionType(statement.expression)

            is BlockStatement -> {
                val previousEnvironment = environment
                environment = SemanticEnvironment(previousEnvironment)

                for (innerStatement in statement.statements) {
                    visitStatement(innerStatement)
                }

                collectScopeWarnings(environment)

                environment = previousEnvironment
            }

            is IfStatement -> {
                val conditionType = inferExpressionType(statement.condition)
                if (conditionType != null && conditionType != SemanticType.BOOLEAN) {
                    _errors.add("If condition must be Boolean, got $conditionType.")
                }
                visitStatement(statement.thenBranch)
                if (statement.elseBranch != null) {
                    visitStatement(statement.elseBranch)
                }
            }

            is WhileStatement -> {
                val conditionType = inferExpressionType(statement.condition)
                if (conditionType != null && conditionType != SemanticType.BOOLEAN) {
                    _errors.add("While condition must be Boolean, got $conditionType.")
                }
                visitStatement(statement.body)
            }

            else -> _errors.add("Unsupported statement type: ${statement::class.simpleName}")
        }
    }

    fun visitExpression(expression: Expression) {
        inferExpressionType(expression)
    }

    private fun inferExpressionType(expression: Expression): SemanticType? {
        return when (expression) {
            is NumberExpression -> SemanticType.NUMBER
            is StringExpression -> SemanticType.STRING
            is BooleanExpression -> SemanticType.BOOLEAN

            is VariableExpression -> {
                val variableType = environment.getVariableType(expression.name)
                if (variableType == null) {
                    _errors.add("Variable '${expression.name}' is not defined.")
                    null
                } else {
                    if (environment.isVariableInitialized(expression.name) == false) {
                        _errors.add("Variable '${expression.name}' is not initialized.")
                    }
                    environment.markVariableRead(expression.name)
                    variableType
                }
            }

            is AssignExpression -> {
                val variableType = environment.getVariableType(expression.name)
                val valueType = inferExpressionType(expression.value)

                if (variableType == null) {
                    _errors.add("Variable '${expression.name}' is not defined.")
                    null
                } else {
                    if (valueType != null && variableType != valueType) {
                        _errors.add(
                            "Type mismatch in assignment to '${expression.name}': expected $variableType, got $valueType."
                        )
                    }

                    environment.markVariableWritten(expression.name)
                    variableType
                }
            }

            is BinaryExpression -> {
                val leftType = inferExpressionType(expression.left)
                val rightType = inferExpressionType(expression.right)
                inferBinaryType(expression.operator.name, leftType, rightType)
            }

            is UnaryExpression -> {
                val rightType = inferExpressionType(expression.right)
                inferUnaryType(expression.operator.name, rightType)
            }

            else -> {
                _errors.add("Unsupported expression type: ${expression::class.simpleName}")
                null
            }
        }
    }

    private fun inferBinaryType(operator: String, left: SemanticType?, right: SemanticType?): SemanticType? {
        if (left == null || right == null) {
            return null
        }

        return when (operator) {
            "PLUS" -> {
                when {
                    left == SemanticType.NUMBER && right == SemanticType.NUMBER -> SemanticType.NUMBER
                    left == SemanticType.STRING && right == SemanticType.STRING -> SemanticType.STRING
                    else -> {
                        _errors.add("Operator '+' is not defined for $left and $right.")
                        null
                    }
                }
            }

            "MINUS", "STAR", "SLASH" -> {
                if (left == SemanticType.NUMBER && right == SemanticType.NUMBER) {
                    SemanticType.NUMBER
                } else {
                    _errors.add("Operator '${operatorToSymbol(operator)}' requires Number operands, got $left and $right.")
                    null
                }
            }

            "LT", "LTEQ", "GT", "GTEQ" -> {
                if (left == SemanticType.NUMBER && right == SemanticType.NUMBER) {
                    SemanticType.BOOLEAN
                } else {
                    _errors.add(
                        "Operator '${operatorToSymbol(operator)}' requires Number operands, got $left and $right."
                    )
                    null
                }
            }

            "EQEQ", "NEQ" -> {
                if (left == right) {
                    SemanticType.BOOLEAN
                } else {
                    _errors.add(
                        "Operator '${operatorToSymbol(operator)}' requires operands of the same type, got $left and $right."
                    )
                    null
                }
            }

            "AND", "OR" -> {
                if (left == SemanticType.BOOLEAN && right == SemanticType.BOOLEAN) {
                    SemanticType.BOOLEAN
                } else {
                    _errors.add(
                        "Operator '${operatorToSymbol(operator)}' requires Boolean operands, got $left and $right."
                    )
                    null
                }
            }

            else -> {
                _errors.add("Unsupported binary operator '$operator'.")
                null
            }
        }
    }

    private fun inferUnaryType(operator: String, right: SemanticType?): SemanticType? {
        if (right == null) {
            return null
        }

        return when (operator) {
            "MINUS" -> {
                if (right == SemanticType.NUMBER) {
                    SemanticType.NUMBER
                } else {
                    _errors.add("Unary '-' requires Number operand, got $right.")
                    null
                }
            }

            "EXCL" -> {
                if (right == SemanticType.BOOLEAN) {
                    SemanticType.BOOLEAN
                } else {
                    _errors.add("Unary '!' requires Boolean operand, got $right.")
                    null
                }
            }

            else -> {
                _errors.add("Unsupported unary operator '$operator'.")
                null
            }
        }
    }

    private fun operatorToSymbol(operator: String): String {
        return when (operator) {
            "PLUS" -> "+"
            "MINUS" -> "-"
            "STAR" -> "*"
            "SLASH" -> "/"
            "LT" -> "<"
            "LTEQ" -> "<="
            "GT" -> ">"
            "GTEQ" -> ">="
            "EQEQ" -> "=="
            "NEQ" -> "!="
            "AND" -> "&&"
            "OR" -> "||"
            "EXCL" -> "!"
            else -> operator
        }
    }

    private fun collectScopeWarnings(scope: SemanticEnvironment) {
        for (unusedVariable in scope.getUnusedVariablesInCurrentScope()) {
            _warnings.add("Variable '$unusedVariable' is declared but never used.")
        }
    }

    val errors: List<String>
        get() = _errors

    val warnings: List<String>
        get() = _warnings
}