package ft.etachott

class Reporter {
    var hadError: Boolean = false

    fun error(line: Int, message: String) = report(line, "", message)

    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] $where: $message")
        hadError = true
    }
}