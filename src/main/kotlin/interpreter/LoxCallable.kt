package ft.etachott.interpreter

interface LoxCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}