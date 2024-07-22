package ft.etachott

import java.io.File
import java.util.*
import kotlin.system.exitProcess

class Runner {
    val reporter = Reporter()

    fun runPrompt() {
        while (true) {
            print(">>> ")
            val line = readlnOrNull() ?: break
            runCode(line)
            reporter.hadError = false
        }
    }

    fun runFile(path: String) {
        runCode(File(path).readText())
        if (reporter.hadError) {
            exitProcess(1)
        }
    }

    private fun runCode(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.tokens()

        tokens.forEach {
            println("Token == $it")
        }
    }
}