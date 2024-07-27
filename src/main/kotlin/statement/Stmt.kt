package ft.etachott.statement

import ft.etachott.expression.Expr
import ft.etachott.tokens.Token

sealed class Stmt {
    interface Visitor<R> {
        fun visitExpressionStmt(stmt: Expression?): R
        fun visitPrintStmt(stmt: Print?): R
        fun visitLetStmt(stmt: Let?): R
    }
    data class Expression (
        val expression: Expr?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitExpressionStmt(this) 
    }
    data class Print (
        val expression: Expr,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitPrintStmt(this) 
    }
    data class Let (
        val name: Token,
        val initializer: Expr?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitLetStmt(this) 
    }

    abstract fun <R> accept(visitor: Visitor<R>): R
}