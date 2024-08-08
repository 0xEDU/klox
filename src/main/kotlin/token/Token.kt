package ft.etachott.tokens

import ft.etachott.enums.TokenType

class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val tokenLine: Int,
) {
    override fun toString(): String = "$type $lexeme $literal"
}