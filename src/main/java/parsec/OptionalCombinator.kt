package parsec

import tools.Either

class OptionalCombinator<T, out R>(private val parser: Parser<T, R>) : Parser<T, R?>() {
    override fun parse(tokens: Sequence<T>): ParserResult<T, R?> = parser.parse(tokens).let {
        when (it) {
            is Either.Left -> Either.right(null to tokens)
            is Either.Right -> it
        }
    }
}

fun <T, R> optional(parser: () -> Parser<T, R>) = OptionalCombinator(parser())