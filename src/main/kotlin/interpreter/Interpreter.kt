package ft.etachott.interpreter

import ft.etachott.errors.ErrorReporter
import ft.etachott.errors.RuntimeError
import ft.etachott.expression.Expr
import ft.etachott.tokens.Token
import ft.etachott.tokens.TokenType

class Interpreter(
    private val errorReporter: ErrorReporter
) : Expr.Visitor<Any?> {
    private fun checkNumberOperand(operator: Token, operand: Any?) = when (operand) {
        is Double -> {}
        else -> throw RuntimeError(operator, "Operand must be a number.")
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
            TokenType.BANG_EQUAL -> isEqual(left, right)
            TokenType.EQUAL_EQUAL -> !isEqual(left, right)
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

    private fun evaluate(expr: Expr?) = expr!!.accept(this)

    fun interpret(expression: Expr?) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            errorReporter.runtimeError(error)
        }
    }
}