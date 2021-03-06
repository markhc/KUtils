package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * ChoiceArg takes in any number of arbitrary objects and maps arguments that match it's toString() string
 * Example: the companion object allows two choices "true" or "false" and will automatically convert to Boolean
 */
open class ChoiceArg(override val name: String, vararg choices: Any) : ArgumentType<String>() {
    companion object BinaryChoiceArg : ChoiceArg("Choice", true, false)

    private val enumerations = choices.associateBy { it.toString().toLowerCase() }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        val selection = enumerations[arg.toLowerCase()] as? String
            ?: return ArgumentResult.Error("Invalid choice. Available choices: ${enumerations.keys.joinToString(", ")}")

        return ArgumentResult.Success(selection)
    }

    override fun generateExamples(event: CommandEvent<*>) = enumerations.keys.toList()
}