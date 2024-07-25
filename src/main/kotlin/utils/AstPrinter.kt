package ft.etachott.utils

import ft.etachott.expression.Expr

class AstPrinter : Expr.Visitor<String> {
    private fun parenthesize(lexeme: String, vararg exprs: Expr?): String {
        val builder = StringBuilder()

        builder.append("(")
        exprs.forEach {
            builder.append(" ")
            builder.append(it?.accept(this) ?: "nil")
        }
        builder.append(")")

        return builder.toString()
    }

    fun print(expr: Expr): String = expr.accept(this)

    override fun visitBinaryExpr(expr: Expr.Binary?): String = parenthesize(
        expr!!.operator.lexeme,
        expr.left,
        expr.right
    )

    override fun visitGroupingExpr(expr: Expr.Grouping?): String = parenthesize("group", expr!!.expression)

    override fun visitLiteralExpr(expr: Expr.Literal?): String = expr?.value?.toString() ?: "nil"

    override fun visitUnaryExpr(expr: Expr.Unary?): String = parenthesize(expr!!.operator.lexeme, expr.right)
}