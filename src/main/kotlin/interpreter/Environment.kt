package ft.etachott.interpreter

import ft.etachott.errors.RuntimeError
import ft.etachott.tokens.Token

class Environment {
    private val environment: MutableMap<String, Any?> = mutableMapOf()

    fun define(key: String, value: Any?) = environment.put(key, value)

    operator fun get(key: Token): Any? =
        if (environment.containsKey(key.lexeme)) environment[key.lexeme]
        else throw RuntimeError(key, "Undefined variable \'${key.lexeme}\'.")
}
