package ft.etachott.parser

import ft.etachott.errors.ErrorReporter
import ft.etachott.expression.Expr
import ft.etachott.statement.Stmt
import ft.etachott.tokens.Token
import ft.etachott.tokens.TokenType
import kotlin.math.exp

class Parser(
    val tokens: List<Token>,
    val errorReporter: ErrorReporter
) {
    private class ParserError() : RuntimeException()

    private var current = 0

    private fun previous(): Token = tokens[current - 1]

    private fun peek(): Token = tokens[current]

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: TokenType): Boolean = if (isAtEnd()) false else peek().type == type

    private fun match(vararg types: TokenType): Boolean {
        types.forEach {
            if (check(it)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun parserError(token: Token, message: String): ParserError {
        errorReporter.error(token, message)
        return ParserError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS,
                TokenType.FUN,
                TokenType.LET,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN -> return
                else -> {}
            }
            advance()
        }
    }

    // Expressions
    private fun consume(type: TokenType, message: String): Token =
        if (check(type))
            advance()
        else
            throw parserError(peek(), message)

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            throw parserError(equals, "Invalid assignment target")
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while(match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
    }

    private fun call(): Expr {
        val finishedCall = { callee: Expr ->
            val arguments = mutableListOf<Expr>()

            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    if (arguments.size >= 255) {
                        throw parserError(peek(), "Can't have more than 255 params")
                    }
                    arguments.addLast(expression())
                } while (match(TokenType.COMMA))
            }
            val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
            Expr.Call(callee, paren, arguments)
        }
        var expr = primary()

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishedCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())


        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
         throw parserError(peek(), "Expect expression.")
    }
    // !Expression

    // Statements
    private fun declaration(): Stmt? {
        return try {
            if (match(TokenType.FUN)) function("function")
            else if (match(TokenType.LET)) letDeclaration()
            else statement()
        } catch (error: ParserError) {
            synchronize()
            null
        }
    }

    private fun letDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val initializer: Expr? = if (match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Let(name, initializer)
    }

    private fun statement(): Stmt =
        if (match(TokenType.PRINT)) printStatement()
        else if (match(TokenType.RETURN)) returnStatement()
        else if (match(TokenType.WHILE)) whileStatement()
        else if (match(TokenType.FOR)) forStatement()
        else if (match(TokenType.IF)) ifStatement()
        else if (match(TokenType.LEFT_BRACE)) Stmt.Block(block())
        else expressionStatement()

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer =
            if (match(TokenType.SEMICOLON)) null
            else if (match(TokenType.LET)) letDeclaration()
            else expressionStatement()

        var condition = if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        val increment = if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()
        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) body = Stmt.Block(listOf(initializer, body))

        return body
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'if' condition.")

        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) statement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value: Expr? = if (!check(TokenType.SEMICOLON)) expression() else null

        consume(TokenType.SEMICOLON,  "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun function(kind: String): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect '$kind' name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    throw parserError(peek(), "Can't have more than 255 params")
                }

                parameters.addLast(
                    consume(TokenType.IDENTIFIER, "Expect parameters name.")
                )
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.addLast(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }
    // !Statements

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!isAtEnd()) {
            statements.addLast(declaration())
        }

        return statements
    }
}