package arguments

import me.jakejmattson.kutils.api.arguments.CharArg
import utilities.ArgumentTestFactory

class CharArgTest : ArgumentTestFactory {
    override val argumentType = CharArg

    override val validArgs = listOf(
        "a" to 'a',
        "1" to '1'
    )

    override val invalidArgs = listOf("abc", "")
}
