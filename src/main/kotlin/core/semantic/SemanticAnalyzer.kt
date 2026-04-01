package core.semantic

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
                val isDefinedInOuterScope = environment.isVariableDefinedInOuterScopes(statement.name)

                if (statement.typeAnnotation == null) {
                    _errors.add("Variable '${statement.name}' requires an explicit type annotation (e.g. var ${statement.name}: Number | String | Boolean = ...).")
                    return
                }

                val declaredType = tokenTypeToValueType(statement.typeAnnotation)

                if (statement.initializer != null) {
                    val initializerType = inferExpressionType(statement.initializer) ?: return
                    if (initializerType != declaredType) {
                        _errors.add("Type mismatch in '${statement.name}': declared ${declaredType.name} but initializer has type ${initializerType.name}.")
                        return
                    }
                }

                val isInitialized = statement.initializer != null

                if (!environment.defineVariable(statement.name, declaredType, isInitialized = isInitialized)) {
                    _errors.add("Variable '${statement.name}' is already defined.")
                } else if (isDefinedInOuterScope) {
                    _warnings.add("Variable '${statement.name}' shadows a variable from an outer scope.")
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
                ensureType(conditionType, ValueType.BOOLEAN, "If condition must be BOOLEAN.")

                visitStatement(statement.thenBranch)
                if (statement.elseBranch != null) {
                    visitStatement(statement.elseBranch)
                }
            }

            is WhileStatement -> {
                val conditionType = inferExpressionType(statement.condition)
                ensureType(conditionType, ValueType.BOOLEAN, "While condition must be BOOLEAN.")
                visitStatement(statement.body)
            }

            else -> _errors.add("Unsupported statement type: ${statement::class.simpleName}")
        }
    }

    private fun inferExpressionType(expression: Expression): ValueType? {
        return when (expression) {
            is NumberExpression -> ValueType.NUMBER
            is StringExpression -> ValueType.STRING
            is BooleanExpression -> ValueType.BOOLEAN

            is VariableExpression -> {
                if (!environment.isVariableDefined(expression.name)) {
                    _errors.add("Variable '${expression.name}' is not defined.")
                    null
                } else {
                    if (environment.isVariableInitialized(expression.name) == false) {
                        _errors.add("Variable '${expression.name}' is not initialized.")
                        null
                    } else {
                        environment.markVariableRead(expression.name)
                        environment.getVariableType(expression.name)
                    }
                }
            }

            is AssignExpression -> {
                val valueType = inferExpressionType(expression.value)

                if (!environment.isVariableDefined(expression.name)) {
                    _errors.add("Variable '${expression.name}' is not defined.")
                    null
                } else {
                    val variableType = environment.getVariableType(expression.name)

                    if (variableType != null && valueType != null && variableType != valueType) {
                        _errors.add(
                            "Type mismatch in assignment to '${expression.name}': expected ${variableType.name}, got ${valueType.name}."
                        )
                        null
                    } else {
                        environment.markVariableWritten(expression.name)
                        variableType
                    }
                }
            }

            is BinaryExpression -> inferBinaryType(expression)
            is UnaryExpression -> inferUnaryType(expression)

            else -> {
                _errors.add("Unsupported expression type: ${expression::class.simpleName}")
                null
            }
        }
    }

    private fun inferUnaryType(expression: UnaryExpression): ValueType? {
        val rightType = inferExpressionType(expression.right) ?: return null

        return when (expression.operator) {
            TokenType.EXCL -> {
                ensureType(rightType, ValueType.BOOLEAN, "Unary '!' operator requires BOOLEAN operand.")
                ValueType.BOOLEAN
            }

            TokenType.MINUS -> {
                ensureType(rightType, ValueType.NUMBER, "Unary '-' operator requires NUMBER operand.")
                ValueType.NUMBER
            }

            else -> {
                _errors.add("Unsupported unary operator '${expression.operator}'.")
                null
            }
        }
    }

    private fun inferBinaryType(expression: BinaryExpression): ValueType? {
        val leftType = inferExpressionType(expression.left) ?: return null
        val rightType = inferExpressionType(expression.right) ?: return null

        return when (expression.operator) {
            TokenType.PLUS -> {
                if (leftType == ValueType.NUMBER && rightType == ValueType.NUMBER) {
                    ValueType.NUMBER
                } else if (leftType == ValueType.STRING && rightType == ValueType.STRING) {
                    ValueType.STRING
                } else {
                    _errors.add("Operator '+' supports NUMBER+NUMBER or STRING+STRING, got ${leftType.name}+${rightType.name}.")
                    null
                }
            }

            TokenType.MINUS,
            TokenType.STAR,
            TokenType.SLASH -> {
                if (leftType == ValueType.NUMBER && rightType == ValueType.NUMBER) {
                    ValueType.NUMBER
                } else {
                    _errors.add("Operator '${expression.operator}' requires NUMBER operands, got ${leftType.name} and ${rightType.name}.")
                    null
                }
            }

            TokenType.LT,
            TokenType.LTEQ,
            TokenType.GT,
            TokenType.GTEQ -> {
                if (leftType == ValueType.NUMBER && rightType == ValueType.NUMBER) {
                    ValueType.BOOLEAN
                } else {
                    _errors.add("Comparison '${expression.operator}' requires NUMBER operands, got ${leftType.name} and ${rightType.name}.")
                    null
                }
            }

            TokenType.EQEQ,
            TokenType.NEQ -> {
                if (leftType == rightType) {
                    ValueType.BOOLEAN
                } else {
                    _errors.add("Equality '${expression.operator}' requires matching operand types, got ${leftType.name} and ${rightType.name}.")
                    null
                }
            }

            TokenType.AND,
            TokenType.OR -> {
                if (leftType == ValueType.BOOLEAN && rightType == ValueType.BOOLEAN) {
                    ValueType.BOOLEAN
                } else {
                    _errors.add("Logical operator '${expression.operator}' requires BOOLEAN operands, got ${leftType.name} and ${rightType.name}.")
                    null
                }
            }

            else -> {
                _errors.add("Unsupported binary operator '${expression.operator}'.")
                null
            }
        }
    }

    private fun ensureType(actual: ValueType?, expected: ValueType, message: String) {
        if (actual != null && actual != expected) {
            _errors.add("$message Found ${actual.name}.")
        }
    }

    private fun tokenTypeToValueType(tokenType: TokenType): ValueType = when (tokenType) {
        TokenType.TYPE_NUMBER -> ValueType.NUMBER
        TokenType.TYPE_STRING -> ValueType.STRING
        TokenType.TYPE_BOOLEAN -> ValueType.BOOLEAN
        else -> error("Not a type token: $tokenType")
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
