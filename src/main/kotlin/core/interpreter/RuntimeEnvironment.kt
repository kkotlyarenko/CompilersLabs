package core.interpreter

class RuntimeEnvironment(private val parent: RuntimeEnvironment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: String): Any? {
        if (values.containsKey(name)) {
            return values[name]
        }

        return parent?.get(name)
            ?: throw RuntimeException("Undefined variable '$name'.")
    }

    fun assign(name: String, value: Any?) {
        if (values.containsKey(name)) {
            values[name] = value
            return
        }

        if (parent != null) {
            parent.assign(name, value)
            return
        }

        throw RuntimeException("Undefined variable '$name'.")
    }
}
