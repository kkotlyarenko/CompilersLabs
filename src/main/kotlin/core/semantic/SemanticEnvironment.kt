package org.kkotlyarenko.core.semantic

class SemanticEnvironment(private val parent: SemanticEnvironment? = null) {
    private data class VariableInfo(
        val type: SemanticType,
        var readCount: Int = 0,
        var writeCount: Int = 0,
        var isInitialized: Boolean = false
    )

    private val variables = mutableMapOf<String, VariableInfo>()

    fun defineVariable(name: String, type: SemanticType, initialized: Boolean = false): Boolean {
        if (variables.containsKey(name)) {
            return false
        }

        variables[name] = VariableInfo(type = type, isInitialized = initialized)
        return true
    }

    fun isVariableDefinedInOuterScopes(name: String): Boolean {
        return parent?.isVariableDefined(name) ?: false
    }

    fun markVariableRead(name: String): Boolean {
        val variableInfo = variables[name]
        if (variableInfo != null) {
            variableInfo.readCount++
            return true
        }

        return parent?.markVariableRead(name) ?: false
    }

    fun markVariableWritten(name: String): Boolean {
        val variableInfo = variables[name]
        if (variableInfo != null) {
            variableInfo.writeCount++
            variableInfo.isInitialized = true
            return true
        }

        return parent?.markVariableWritten(name) ?: false
    }

    fun isVariableInitialized(name: String): Boolean? {
        val variableInfo = variables[name]
        if (variableInfo != null) {
            return variableInfo.isInitialized
        }

        return parent?.isVariableInitialized(name)
    }

    fun getVariableType(name: String): SemanticType? {
        val variableInfo = variables[name]
        if (variableInfo != null) {
            return variableInfo.type
        }

        return parent?.getVariableType(name)
    }

    fun getUnusedVariablesInCurrentScope(): List<String> {
        return variables
            .filter { (_, info) -> info.readCount == 0 }
            .keys
            .toList()
    }

    fun isVariableDefined(name: String): Boolean {
        if (variables.containsKey(name)) {
            return true
        }

        return parent?.isVariableDefined(name) ?: false
    }
}
