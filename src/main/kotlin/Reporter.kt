package ft.etachott

import ft.etachott.tokens.Token
import ft.etachott.tokens.TokenType

class Reporter {
    var hadError: Boolean = false

    fun error(token: Token, message: String) =
        if (token.type == TokenType.EOF) {
            report(token.tokenLine, " at end", message)
        } else {
            report(token.tokenLine, " at '${token.lexeme}'", message)
        }

    fun error(line: Int, message: String) = report(line, "", message)

    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] $where: $message")
        hadError = true
    }
}