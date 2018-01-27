package combinator

import parser.Parser
import parser.ParserException
import parser.ParserResult
import parser.plus
import tools.Either

// TODO make sure this is doing the right thing:
fun <T, R> choice(parser: List<Parser<T, R>>) = { tokens: Sequence<T> ->
    var exception: ParserException? = null
    var successful: ParserResult<T, R>? = null
    for (it in parser) {
        val result = it(tokens)
        if (result is Either.Left) {
            exception = exception?.combine(result.value) ?: result.value
        } else if (result is Either.Right) {
            successful = result
            break
        }
    }
    if (successful != null) successful else Either.left(exception)
}

fun <T, R> many(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    var result = emptyList<R>()
    var toks = tokens

    while (true) {
        val parsed = parser(toks)
        if (parsed is Either.Left) {
            break
        } else if (parsed is Either.Right) {
            val (r, ts) = parsed.value
            toks = ts
            result += r
        }
    }

    Either.right(result to toks)
}

fun <T, R> optional(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    parser(tokens).let {
        when (it) {
            is Either.Left -> Either.right(null to tokens)
            is Either.Right -> it
        }
    }
}

infix fun <T, F, S> Parser<T, F>.or(parser: Parser<T, S>) = { tokens: Sequence<T> ->
    val firstResult = this(tokens)
    when (firstResult) {
        is Either.Left -> {
            val secondResult = parser(tokens)
            when (secondResult) {
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