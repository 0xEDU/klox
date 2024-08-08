package ft.etachott.interpreter

import ft.etachott.errors.RuntimeError
import ft.etachott.tokens.Token

class Environment(
    private val enclosing: Environment? = null
) {
    private val values: MutableMap<String, Any?> = mutableMapOf()

    fun define(key: String, value: Any?) = values.put(key, value)

    fun assign(key: Token, value: Any?): Unit =
        if (values.containsKey(key.lexeme))
            values[key.lexeme] = value
        else if (enclosing != null)
            enclosing.assign(key, value)
        else
            throw RuntimeError(key, "Undefined variable '${key.lexeme}'.")

    operator fun get(key: Token): Any? =
        if (values.containsKey(key.lexeme))
            values[key.lexeme]
        else if (enclosing != null)
            enclosing[key]
        else
            throw RuntimeError(key, "Undefined variable \'${key.lexeme}\'.")

    private fun ancestor(distance: Int) = (1..distance).fold(this as Environment?) { environment, _ ->
        environment!!.enclosing
    }

    fun getAt(distance: Int, name: String): Any? = ancestor(distance)!!.values[name]

    fun assignAt(distance: Int, name: Token, value: Any?) = ancestor(distance)!!.values.put(name.lexeme, value)
}
