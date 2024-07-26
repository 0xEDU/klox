package ft.etachott

import ft.etachott.errors.ErrorReporter
import ft.etachott.interpreter.Interpreter
import ft.etachott.parser.Parser
import ft.etachott.scanner.Scanner
import ft.etachott.utils.AstPrinter
import java.io.File
import kotlin.system.exitProcess

class Runner {
    val errorReporter = ErrorReporter()
    val interpreter = Interpreter(errorReporter)

    fun runPrompt() {
        while (true) {
            print(">>> ")
            val line = readlnOrNull() ?: break
            runCode(line)
            errorReporter.hadError = false
        }
    }

    fun runFile(path: String) {
        runCode(File(path).readText())
        if (errorReporter.hadError) exitProcess(1)
        if (errorReporter.hadRuntimeError) exitProcess(2)
    }

    private fun runCode(source: String) {
        val scanner = Scanner(errorReporter)
        val tokens = scanner.scanTokens(source)
        val parser = Parser(tokens, errorReporter)
        val expr = parser.parse()

        if (errorReporter.hadError) return

        interpreter.interpret(expr)
    }
}