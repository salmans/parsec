package combinator

import parser.Parser
import tools.Either

fun <T, F, S> and(left: Parser<T, F>, right: Parser<T, S>): Parser<T, Pair<F, S>> = { tokens: Sequence<T> ->
    val firstResult = left(tokens)
    when (firstResult) {
        is Either.Left -> firstResult
        is Either.Right -> {
            val (fr, fts) = firstResult.value
            val secondResult = right(fts)
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

fun <T, F, S> left(left: Parser<T, F>, right: Parser<T, S>): Parser<T, F> = { tokens: Sequence<T> ->
    val firstResult = left(tokens)
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

fun <T, F, S> right(left: Parser<T, F>, right: Parser<T, S>): Parser<T, S> = { tokens: Sequence<T> ->
    val firstResult = left(tokens)
    when (firstResult) {
        is Either.Left -> firstResult
        is Either.Right -> {
            val (_, fts) = firstResult.value
            right(fts)
        }
    }
}