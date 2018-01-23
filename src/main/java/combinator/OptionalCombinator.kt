package combinator

import parser.Parser
import tools.Either

fun <T, R> optional(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    parser(tokens).let {
        when (it) {
            is Either.Left -> Either.right(null to tokens)
            is Either.Right -> it
        }
    }
}