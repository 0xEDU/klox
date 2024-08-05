package ft.etachott.interpreter.impl

import ft.etachott.interpreter.Environment
import ft.etachott.interpreter.Interpreter
import ft.etachott.interpreter.LoxCallable
import ft.etachott.statement.Stmt

class LoxFunction(
    private val declaration: Stmt.Function?
) : LoxCallable {
    override fun arity(): Int = declaration!!.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)
        declaration!!.params.forEachIndexed { i, p ->
            environment.define(p.lexeme, arguments[i])
        }
        interpreter.executeBlock(declaration.body, environment)
        return null
    }

    override fun toString(): String = "<fn ${declaration!!.name}>"
}