package combinator

import parser.Parser
import tools.Either

infix fun <T, F, S> Parser<T, F>.and(parser: Parser<T, S>): Parser<T, Pair<F, S>> = { tokens: Sequence<T> ->
    val firstResult = this(tokens)
    when (firstResult) {
        is Either.Left -> firstResult
        is Either.Right -> {
            val (fr, fts) = firstResult.value
            val secondResult = parser(fts)
            when (secondResult) {
                is Either.Left -> secondResult
                is Either.Right -> {
                    val (sr, sts) = secondResult.value
                    Either.right((fr to sr) to sts)
                }
            }
        }
    }
}

infix fun <T, F, S> Parser<T, F>.left(right: Parser<T, S>): Parser<T, F> = { tokens: Sequence<T> ->
    val firstResult = this(tokens)
    when (firstResult) {
        is Either.Left -> firstResult
        is Either.Right -> {
            val (fr, fts) = firstResult.value
            val secondResult = right(fts)
            when (secondResult) {
                is Either.Left -> secondResult
                is Either.Right -> {
                    val (_, sts) = secondResult.value
                    Either.right(fr to sts)
                }
            }
        }
    }
}

infix fun <T, F, S> Parser<T, F>.right(parser: Parser<T, S>): Parser<T, S> = { tokens: Sequence<T> ->
    val firstResult = this(tokens)
    when (firstResult) {
        is Either.Left -> firstResult
        is Either.Right -> {
            val (_, fts) = firstResult.value
            parser(fts)
        }
    }
}