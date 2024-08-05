package ft.etachott.interpreter.impl

import ft.etachott.interpreter.Environment
import ft.etachott.interpreter.Interpreter
import ft.etachott.interpreter.LoxCallable
import ft.etachott.interpreter.Return
import ft.etachott.statement.Stmt

class LoxFunction(
    private val declaration: Stmt.Function?,
    private val closure: Environment
) : LoxCallable {
    override fun arity(): Int = declaration!!.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration!!.params.forEachIndexed { i, p ->
            environment.define(p.lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString(): String = "<fn ${declaration!!.name}>"
}