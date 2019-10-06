package me.aberrantfox.kjdautils.internal.command


import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.Manual
import me.aberrantfox.kjdautils.internal.command.Result.Error
import me.aberrantfox.kjdautils.internal.command.Result.Results

const val separatorCharacter = "|"

sealed class Result {
    fun then(function: (List<Any?>) -> Result): Result =
            when (this) {
                is Results -> function(results)
                is Error -> this
            }

    data class Results(val results: ArgumentContainer) : Result()
    data class Error(val error: String) : Result()
}

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): Result {

    val expectedTypes = expected.map { it }

    if (expectedTypes.contains(Manual)) {
        return Results(actual)
    }

    return convertArgs(actual, expected, event)
}

private fun convertOptional(arg: ArgumentType<*>, event: CommandEvent<*>): Any? {
    val default = arg.defaultValue ?: return null

    return when (default) {
        is Function<*> -> (default as (CommandEvent<*>) -> Any).invoke(event)
        else -> default
    }
}

private fun convertArgs(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): Result {

    val remaining = actual.toMutableList()

    var lastError: ArgumentResult.Error<*>? = null
    val converted = expected.map { expectedArg ->
        if (remaining.isEmpty()) {
            if (expectedArg.isOptional)
                convertOptional(expectedArg, event)
            else
                return Error("Missing non-optional argument(s). Try using `help`")
        } else {
            var actualArg = remaining.first()
            while (actualArg.isBlank()) {
                remaining.remove(actualArg)
                actualArg = remaining.first()
            }

            val result = expectedArg.convert(actualArg, remaining.toList(), event)

            when (result) {
                is ArgumentResult.Success -> {
                    if (result.consumed.isNotEmpty())
                        remaining.removeAll(result.consumed)
                    else
                        remaining.remove(actualArg)

                    result.result
                }
                is ArgumentResult.Error -> {
                    lastError = result

                    if (expectedArg.isOptional)
                        convertOptional(expectedArg, event)
                    else
                        return Error(result.error)
                }
            }
        }
    }

    if (remaining.isNotEmpty())
        return Error(lastError?.error ?: "Unmatched arguments. Try using `help`")

    return Results(converted)
}
