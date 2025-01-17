package ft.etachott.resolver

import ft.etachott.enums.FunctionType
import ft.etachott.errors.ErrorReporter
import ft.etachott.expression.Expr
import ft.etachott.interpreter.Interpreter
import ft.etachott.statement.Stmt
import ft.etachott.tokens.Token
import java.util.*

class Resolver(
    private val reporter: ErrorReporter,
    private val interpreter: Interpreter
) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        function.params.forEach {
            declare(it)
            define(it)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    private fun resolve(expr: Expr) = expr.accept(this)

    private fun resolve(stmt: Stmt) = stmt.accept(this)

    fun resolve(statements: List<Stmt>) = statements.forEach(::resolve)

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme))
            reporter.error(name, "Variable already exists ${name.lexeme} in this scope")
        scope[name.lexeme] = false
    }

    private fun define(name: Token) = if (scopes.isEmpty()) null else scopes.peek()[name.lexeme]

    private fun beginScope() = scopes.push(mutableMapOf())

    private fun endScope() = scopes.pop()

    override fun visitAssignExpr(expr: Expr.Assign?) {
        resolve(expr!!.value!!)
        resolveLocal(expr, expr.name)
    }

    override fun visitBinaryExpr(expr: Expr.Binary?) {
        resolve(expr!!.left!!)
        resolve(expr.right!!)
    }

    override fun visitCallExpr(expr: Expr.Call?) {
        resolve(expr!!.callee)
        expr.arguments!!.forEach(::resolve)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping?) = resolve(expr!!.expression!!)

    override fun visitLiteralExpr(expr: Expr.Literal?) {}

    override fun visitLogicalExpr(expr: Expr.Logical?) {
        resolve(expr!!.left!!)
        resolve(expr.right!!)
    }

    override fun visitUnaryExpr(expr: Expr.Unary?) = resolve(expr!!.right!!)

    override fun visitVariableExpr(expr: Expr.Variable?) {
        if (!scopes.isEmpty() && scopes.peek()[expr!!.name.lexeme] == false) {
            reporter.error(expr.name, "Can't read local variable in its own initializer")
        }
        resolveLocal(expr!!, expr.name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block?) {
        beginScope()
        resolve(stmt!!.statements)
        endScope()
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression?) = resolve(stmt!!.expression!!)

    override fun visitFunctionStmt(stmt: Stmt.Function?) {
        declare(stmt!!.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visitIfStmt(stmt: Stmt.If?) {
        resolve(stmt!!.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
    }

    override fun visitLetStmt(stmt: Stmt.Let?) {
        declare(stmt!!.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitPrintStmt(stmt: Stmt.Print?) = resolve(stmt!!.expression)

    override fun visitReturnStmt(stmt: Stmt.Return?) {
        if (currentFunction == FunctionType.NONE)
            reporter.error(stmt!!.keyword, "Can't return from top-level code")
        if (stmt!!.value != null)
            resolve(stmt.value!!)
    }

    override fun visitWhileStmt(stmt: Stmt.While?) {
        resolve(stmt!!.condition)
        resolve(stmt.body!!)
    }

}