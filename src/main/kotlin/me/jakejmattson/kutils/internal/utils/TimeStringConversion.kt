package me.jakejmattson.kutils.internal.utils

import me.jakejmattson.kutils.api.dsl.arguments.ArgumentResult
import me.jakejmattson.kutils.api.extensions.stdlib.isDigitOrPeriod

private typealias Quantity = Double
private typealias Quantifier = String

internal fun convertTimeString(actual: List<String>): ArgumentResult<Double> {
    val timeStringEnd = actual.indexOfFirst { toTimeElement(it) == null }.takeIf { it != -1 } ?: actual.size
    val original = actual.subList(0, timeStringEnd).toList()
    val possibleElements = original.map { toTimeElement(it.toLowerCase()) }
    val timeElements = possibleElements.dropLastWhile { it is Quantity } // assume trailing numbers are part of next arg (ID, Integer, etc.)

    if (timeElements.isEmpty())
        return ArgumentResult.Error("Invalid time element passed.")

    val consumed = original.subList(0, timeElements.size)
    val quantityCount = timeElements.count { it is Quantity }
    val quantifierCount = timeElements.count { it is Quantifier }

    if (quantityCount != quantifierCount)
        return ArgumentResult.Error("The number of quantities doesn't match the number of quantifiers.")

    val hasMissingQuantifier = timeElements.withIndex().any { (index, current) ->
        val next = timeElements.getOrNull(index + 1)
        current is Quantity && next !is Quantifier
    }

    if (hasMissingQuantifier)
        return ArgumentResult.Error("At least one quantity is missing a quantifier.")

    val timePairs = timeElements
        .mapIndexedNotNull { index, element ->
            when (element) {
                is Pair<*, *> -> element as Pair<Quantity, Quantifier>
                is Quantity -> element to timeElements[index + 1] as Quantifier
                else -> null
            }
        }

    if (timePairs.any { it.first < 0.0 })
        return ArgumentResult.Error("Time argument cannot be negative.")

    val timeInSeconds = timePairs
        .map { (quantity, quantifier) -> quantity * timeStringToSeconds.getValue(quantifier) }
        .reduce { a, b -> a + b }

    return ArgumentResult.Success(timeInSeconds, consumed.size)
}

private fun toTimeElement(element: String): Any? = toBoth(element)
    ?: toQuantifier(element)
    ?: toQuantity(element)

private fun toQuantifier(element: String) = element.takeIf { it.toLowerCase() in timeStringToSeconds }
private fun toQuantity(element: String) = element.toDoubleOrNull()

private fun toBoth(element: String): Pair<Double, String>? {
    val quantityRaw = element.toCharArray().takeWhile { it.isDigitOrPeriod() }.joinToString("")
    val quantity = toQuantity(quantityRaw) ?: return null
    val quantifier = toQuantifier(element.substring(quantityRaw.length))
        ?: return null

    return quantity to quantifier
}

private val timeStringToSeconds = mapOf(
    "s" to 1,
    "sec" to 1,
    "second" to 1,
    "seconds" to 1,

    "m" to 60,
    "min" to 60,
    "mins" to 60,
    "minute" to 60,
    "minutes" to 60,

    "h" to 3600,
    "hr" to 3600,
    "hrs" to 3600,
    "hour" to 3600,
    "hours" to 3600,

    "d" to 86400,
    "day" to 86400,
    "days" to 86400,

    "w" to 604800,
    "week" to 604800,
    "weeks" to 604800,

    "month" to 2592000,
    "months" to 2592000,

    "y" to 31536000,
    "yr" to 31536000,
    "yrs" to 31536000,
    "year" to 31536000,
    "years" to 31536000
)