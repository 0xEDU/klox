package ft.etachott.scanner

import ft.etachott.tokens.Token
import ft.etachott.tokens.TokenType

class Scanner(
    private val source: String,
) {
    private var tokens: MutableList<Token> = mutableListOf()
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
        }
    }

    fun scanTokens(): List<Token> {
        while(!isAtEnd()) {
            start = current;
            scanToken()
            tokens.addLast(Token(TokenType.EOF, "", null, line))
        }
        return tokens
    }
}