package combinator

import parser.Parser
import parser.plus
import tools.Either

fun <T, F, S> or(left: Parser<T, F>, right: Parser<T, S>) = { tokens: Sequence<T> ->
    val firstResult = left(tokens)
    when (firstResult) {
        is Either.Left -> {
            val secondResult = right(tokens)
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