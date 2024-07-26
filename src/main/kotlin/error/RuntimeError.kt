package ft.etachott.errors

import ft.etachott.tokens.Token

class RuntimeError(val token: Token, message: String) : RuntimeException(message) {
}