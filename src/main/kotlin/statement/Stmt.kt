package ft.etachott.statement

import ft.etachott.expression.Expr
import ft.etachott.tokens.Token

sealed class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block?): R
        fun visitExpressionStmt(stmt: Expression?): R
        fun visitIfStmt(stmt: If?): R
        fun visitLetStmt(stmt: Let?): R
        fun visitPrintStmt(stmt: Print?): R
        fun visitWhileStmt(stmt: While?): R
    }
    data class Block (
        val statements: List<Stmt>,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBlockStmt(this) 
    }
    data class Expression (
        val expression: Expr?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitExpressionStmt(this) 
    }
    data class If (
        val condition: Expr,
        val thenBranch: Stmt,
        val elseBranch: Stmt?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitIfStmt(this) 
    }
    data class Let (
        val name: Token,
        val initializer: Expr?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitLetStmt(this) 
    }
    data class Print (
        val expression: Expr,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitPrintStmt(this) 
    }
    data class While (
        val condition: Expr,
        val body: Stmt?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitWhileStmt(this) 
    }

    abstract fun <R> accept(visitor: Visitor<R>): R
}