package ft.etachott

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val runner = Runner();
    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(1)
    } else if (args.size == 1) {
        runner.runFile(args[0])
    } else {
        runner.runPrompt()
    }
}