package org.kkotlyarenko.core.semantic

import org.kkotlyarenko.core.parser.ast.*

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
                if (statement.initializer != null) {
                    visitExpression(statement.initializer)
                }

                val isDefinedInOuterScope = environment.isVariableDefinedInOuterScopes(statement.name)

                if (!environment.defineVariable(statement.name)) {
                    _errors.add("Variable '${statement.name}' is already defined.")
                } else {
                    if (isDefinedInOuterScope) {
                        _warnings.add("Variable '${statement.name}' shadows a variable from an outer scope.")
                    }

                    if (statement.initializer != null) {
                        environment.markVariableWritten(statement.name)
                    }
                }
            }

            is PrintStatement -> visitExpression(statement.expression)
            is ExpressionStatement -> visitExpression(statement.expression)

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
                visitExpression(statement.condition)
                visitStatement(statement.thenBranch)
                if (statement.elseBranch != null) {
                    visitStatement(statement.elseBranch)
                }
            }

            is WhileStatement -> {
                visitExpression(statement.condition)
                visitStatement(statement.body)
            }

            else -> _errors.add("Unsupported statement type: ${statement::class.simpleName}")
        }
    }

    fun visitExpression(expression: Expression) {
        when (expression) {
            is NumberExpression, is StringExpression -> Unit

            is VariableExpression -> {
                if (!environment.isVariableDefined(expression.name)) {
                    _errors.add("Variable '${expression.name}' is not defined.")
                } else {
                    if (environment.isVariableInitialized(expression.name) == false) {
                        _errors.add("Variable '${expression.name}' is not initialized.")
                    }
                    environment.markVariableRead(expression.name)
                }
            }

            is AssignExpression -> {
                visitExpression(expression.value)
                if (!environment.markVariableWritten(expression.name)) {
                    _errors.add("Variable '${expression.name}' is not defined.")
                }
            }

            is BinaryExpression -> {
                visitExpression(expression.left)
                visitExpression(expression.right)
            }

            is UnaryExpression -> visitExpression(expression.right)

            else -> _errors.add("Unsupported expression type: ${expression::class.simpleName}")
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