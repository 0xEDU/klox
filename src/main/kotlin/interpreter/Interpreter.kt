package ft.etachott.interpreter

import ft.etachott.errors.ErrorReporter
import ft.etachott.errors.RuntimeError
import ft.etachott.expression.Expr
import ft.etachott.statement.Stmt
import ft.etachott.tokens.Token
import ft.etachott.tokens.TokenType

class Interpreter(
    private val errorReporter: ErrorReporter
) : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private var environment = Environment()

    private fun checkNumberOperand(operator: Token, operand: Any?) = when (operand) {
        is Double -> {}
        else -> throw RuntimeError(operator, "Operand must be a number.")
    }

    override fun visitLogicalExpr(expr: Expr.Logical?): Any? {
        val left = evaluate(expr!!.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) =
        if (left is Double && right is Double) {}
        else throw RuntimeError(operator, "Operands must be a number.")

    private fun isTruthy(any: Any?) = when (any) {
        null -> false
        is Boolean -> any
        else -> true
    }

    private fun isEqual(a: Any?, b: Any?) = when (a) {
        (a == null && b == null) -> true
        (a == null) -> false
        else -> a == b
    }

    private fun stringify(obj: Any?) = when (obj) {
        (obj == null) -> "nil"
        (obj is Double) -> {
            val text = obj.toString()
            if (text.endsWith(".0")) {
                text.substring(0, text.length - 2)
            } else {
                text
            }
        }
        else -> obj.toString()
    }

    private fun execute(stmt: Stmt) = stmt.accept(this)

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            statements.forEach {
                execute(it)
            }
        } finally {
            this.environment = previous
        }
    }

    private fun evaluate(expr: Expr?) = expr!!.accept(this)

    override fun visitAssignExpr(expr: Expr.Assign?): Any? {
        val value = evaluate(expr!!.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary?): Any? {
        val left = evaluate(expr!!.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.operator, "Operands must be a number.")
                }
            }
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            else -> null
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping?): Any? = evaluate(expr!!.expression)

    override fun visitLiteralExpr(expr: Expr.Literal?): Any? = expr!!.value

    override fun visitUnaryExpr(expr: Expr.Unary?): Any? {
        val right = evaluate(expr!!.right)

        return when (expr.operator.type) {
            TokenType.BANG -> {
                checkNumberOperand(expr.operator, right)
                !isTruthy(right)
            }
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            else -> null
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable?): Any? = environment[expr!!.name]

    override fun visitBlockStmt(stmt: Stmt.Block?) {
        executeBlock(stmt!!.statements, Environment(environment))
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression?) {
        evaluate(stmt!!.expression)
    }

    override fun visitIfStmt(stmt: Stmt.If?) {
        if (isTruthy(evaluate(stmt!!.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print?) {
        val value = evaluate(stmt!!.expression)
        println(stringify(value))
    }

    override fun visitWhileStmt(stmt: Stmt.While?) {
        while (isTruthy(evaluate(stmt!!.condition))) {
            execute(stmt.body!!)
        }
    }

    override fun visitLetStmt(stmt: Stmt.Let?) {
        val value: Any? = if (stmt?.initializer == null) null else evaluate(stmt.initializer)
        environment.define(stmt!!.name.lexeme, value)
    }

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach {
                execute(it)
            }
        } catch (error: RuntimeError) {
            errorReporter.runtimeError(error)
        }
    }
}