package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class IntegerRangeArg(private val min: Int, private val max: Int, override val name: String = "Integer ($min-$max)") : ArgumentType<Int>() {

    init {
        require(max > min) { "Maximum value must be greater than minimum value." }
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {
        val integerArg = arg.toIntOrNull() ?: return ArgumentResult.Error("Argument must be an integer.")

        if (integerArg !in min..max) return ArgumentResult.Error("Argument not in range $min-$max.")

        return ArgumentResult.Success(integerArg)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf((min..max).random().toString())
}
