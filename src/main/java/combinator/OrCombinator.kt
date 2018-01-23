package combinator

import parser.Parser
import parser.plus
import tools.Either

infix fun <T, F, S> Parser<T, F>.or(parser: Parser<T, S>) = { tokens: Sequence<T> ->
    val firstResult = this(tokens)
    when (firstResult) {
        is Either.Left -> {
            val secondResult = parser(tokens)
            when(secondResult) {
                is Either.Left -> Either.left(firstResult.value + secondResult.value)
                is Either.Right -> {
                    val (sr, sts) = secondResult.value
                    Either.right(Either.right(sr) to sts)
                }
            }

        }
        is Either.Right -> {
            val (fr, fts) = firstResult.value
            Either.right(Either.left(fr) to fts)
        }
    }
}