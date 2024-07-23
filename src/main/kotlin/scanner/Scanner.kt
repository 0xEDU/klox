package ft.etachott.scanner

import ft.etachott.Reporter
import ft.etachott.tokens.Token
import ft.etachott.tokens.TokenType

class Scanner(
    private val source: String,
    private val reporter: Reporter,
) {
    var tokens: MutableList<Token> = mutableListOf()
    private var start = 0
    private var current = 0
    private var line = 1

    private fun isAtEnd(): Boolean = current >= source.length

    private fun advance(): Char = source[current++]

    private fun addToken(token: TokenType) = addToken(token, null)

    private fun addToken(token: TokenType, literal: Any?) {
        val text: String = source.substring(start, current)
        tokens.add(Token(token, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        return if (isAtEnd() || source[current] != expected) false
        else {
            current++
            true
        }
    }

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]

    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]

    private fun scanToken() {
        val c: Char = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.LEFT_PAREN)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) advance()
            } else {
                addToken(TokenType.SLASH)
            }

            '"' -> string()
            in '0'..'9' -> number()
            '\n' -> line++
            ' ', '\r', '\t' -> {}
            else -> reporter.error(line, "Unrecognized character: $c")
        }
    }

    private fun number() {
        while (peek().isDigit()) advance()
        if (peek() == '.' && peekNext().isDigit()) {
            advance()
            while (peek().isDigit()) advance()
        }
        addToken(
            TokenType.NUMBER,
            source.substring(start, current).toDouble()
        )
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            reporter.error(line, "Unterminated string")
            return
        }
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current;
            scanToken()
            tokens.addLast(Token(TokenType.EOF, "", null, line))
        }
        return tokens
    }
}