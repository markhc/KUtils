package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class QuoteArg(override val name: String = "Quote") : ArgumentType<String>() {
    companion object : QuoteArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {

        val quotationMark = '"'

        if (!arg.startsWith(quotationMark)) {
            return ArgumentResult.Error("Expected an opening quotation mark, found: $arg")
        }

        val rawQuote = if (arg.endsWith(quotationMark)) {
            arg
        } else {
            args.takeUntil { !it.endsWith(quotationMark) }.joinToString(" ")
        }

        if (!rawQuote.endsWith(quotationMark)) {
            return ArgumentResult.Error("Missing closing quotation mark.")
        }

        val quote = rawQuote.trim(quotationMark)
        val consumedCount = quote.split(" ").size

        return ArgumentResult.Success(quote, consumedCount)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("\"A Quote\"")
}

private fun List<String>.takeUntil(predicate: (String) -> Boolean): List<String> {
    val result = this.takeWhile(predicate).toMutableList()
    val index = result.size

    if (index in indices)
        result.add(this[index])

    return result
}