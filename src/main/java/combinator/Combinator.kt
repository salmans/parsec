package combinator

import parser.*
import tools.Either

/**
 * Implements mplus (<|>): it returns the result of the first parser if it runs successfully.
 * It returns the result of the second parser if the first parser fails without consuming any tokens.
 */
infix fun <T, R> Parser<T, R>.or(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    val (fr, fts) = this(tokens)
    when (fr) {
        is Either.Left -> {
            if (fts == tokens) {
                val (sr, sts) = parser(tokens)
                when (sr) {
                    is Either.Left -> Either.Left(fr.value + sr.value) to tokens
                    is Either.Right -> sr to sts
                }
            } else {
                fr to fts
            }
        }
        is Either.Right -> Either.right(fr.value) to fts
    }
}

/**
 * Applies `parser` without consuming the input. It fails if `parser` does.
 */
fun <T, R> lookAhead(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    parser(tokens).first to tokens
}

/**
 * Acts like `parser` but pretends that it has not consumed the input when it fails.
 * This is the conventional `try` parser in Parsec.
 */
fun <T, R> attempt(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    val (fr, fts) = parser(tokens)
    when (fr) {
        is Either.Left -> fr to tokens
        is Either.Right -> fr to fts
    }
}

/**
 * Applies `parser` many times until it cannot be applied any more and returns the result in a list.
 * It fails if `parser` fails after consuming tokens.
 */
fun <T, R> many(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    var exception: ParserException? = null
    var result = emptyList<R>()
    var toks = tokens

    while (true) {
        val (r, ts) = parser(toks)
        if (r is Either.Left) {
            if (ts != toks) {
                exception = r.value
                toks = ts
            }
            break
        } else if (r is Either.Right) {
            result += r.value
            toks = ts
        }
    }

    if (exception != null) {
        Either.left(exception) to toks
    } else {
        Either.right(result) to toks
    }
}

/**
 * Applies `parser` one or more times until it cannot be applied any more and returns the result in a list.
 * It fails if `parser` fails after consuming tokens.
 */
fun <T, R> many1(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    (parser and many(parser))(tokens).mapResult { listOf(it.first) + it.second }
}

/**
 * Applies `parser` many times until it cannot be applied any more, discarding the result.
 */
fun <T, R> skipMany(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    many(parser)(tokens).mapResult { Unit }
}

/**
 * Applies `parser` one or many times until it cannot be applied any more, discarding the result.
 */
fun <T, R> skipMany1(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    (parser and many(parser))(tokens).mapResult { Unit }
}

/**
 * Applies the parsers in the input list; returns the result of the first successful parser. It fails
 * if any of the parsers fail after consuming some tokens.
 */
fun <T, R> choice(parsers: List<Parser<T, R>>) = { tokens: Sequence<T> ->
    var exception: Pair<ParserException, Sequence<T>>? = null
    if (parsers.isNotEmpty()) {
        var successful: ParserResult<T, R>? = null
        for (it in parsers) {
            val parsed = it(tokens)
            val parsedResult = parsed.first
            if (parsedResult is Either.Left) {
                exception = (exception?.first?.plus(parsedResult.value) ?: parsedResult.value) to parsed.second
            } else if (parsedResult is Either.Right) {
                successful = parsed
                break
            }
        }
        if (successful != null) successful else exception!!.let { Either.left(it.first) to it.second }
    } else {
        Either.left(ParserException("Cannot apply the `choice` an empty list of parsers.")) to tokens
    }
}

/**
 * Applies `parser` for `n` times and returns the result. It fails if `parser` fails after consuming tokens.
 */
fun <T, R> count(n: Int, parser: Parser<T, R>) = { tokens: Sequence<T> ->
    var result = emptyList<R>()
    var exception: Pair<ParserException, Sequence<T>>? = null
    var toks = tokens

    if (n > 0) {
        for (i in 0..(n - 1)) {
            val parsed = parser(toks)
            val parsedResult = parsed.first
            if (parsedResult is Either.Left) {
                exception = parsedResult.value to parsed.second
                break
            } else if (parsedResult is Either.Right) {
                toks = parsed.second
                result += listOf(parsedResult.value)
            }
        }

        if (exception == null) {
            Either.right(result) to toks
        } else {
            exception.let { Either.left(it.first) to it.second }
        }
    } else {
        Either.right(emptyList<R>()) to tokens
    }
}

/**
 * Parses `parser` between `open` and `close`.
 */
fun <T, O, C, R> between(open: Parser<T, O>, close: Parser<T, C>, parser: Parser<T, R>) = { tokens: Sequence<T> ->
    (open and parser and close)(tokens).let {
        it.mapResult { it.first.second }
    }
}

/**
 * Returns the result of `parser` as an optional type and `null` if `parser` cannot be applied.
 * It fails if `parser` fails after consuming tokens.
 */
fun <T, R> optionMaybe(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    parser(tokens).let {
        val (r, ts) = it
        when (r) {
            is Either.Left -> if (tokens == ts) {
                Either.right(null) to ts
            } else {
                it
            }
            is Either.Right -> Either.right(r.value) to ts
        }
    }
}

/**
 * Applies `parser` and discards the result of `parser` if it is successful. It fails if `parser` fails after
 * consuming tokens.
 */
fun <T, R> optional(parser: Parser<T, R>) = { tokens: Sequence<T> ->
    optionMaybe(parser)(tokens).let {
        it.mapResult { Unit }
    }
}

/**
 * Applies `parser` and returns the result of `parser` if it is successful and `default` if `parser` cannot
 * be applied. It fails if `parser` fils after consuming tokens.
 */
fun <T, R> option(default: R, parser: Parser<T, R>) = { tokens: Sequence<T> ->
    optionMaybe(parser)(tokens).let {
        it.mapResult { it ?: default }
    }
}

/**
 * Parses `parser` one or many times, separated by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepBy1(parser: Parser<T, R>, sep: Parser<T, S>) = { tokens: Sequence<T> ->
    (parser and many(sep right parser))(tokens).mapResult {
        if (it.first != null) {
            listOf(it.first) + it.second
        } else {
            emptyList()
        }
    }
}

/**
 * Parses `parser` zero or many times, separated by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepBy(parser: Parser<T, R>, sep: Parser<T, S>) = sepBy1(parser, sep) or give(emptyList())

/**
 * Parses `parser` one or many times, separated and ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> endBy1(parser: Parser<T, R>, sep: Parser<T, S>) = { tokens: Sequence<T> ->
    many1(parser and sep)(tokens).mapResult {
        it.map { it.first }
    }
}

/**
 * Parses `parser` zero or many times, separated and ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> endBy(parser: Parser<T, R>, sep: Parser<T, S>) = { tokens: Sequence<T> ->
    many(parser and sep)(tokens).mapResult {
        it.map { it.first }
    }
}

/**
 * Parses `parser` one or many times, separated and ended by `sep` and returns the results of parsing `parser` in a list.
 * do{ x <- p
; do{ sep
; xs <- sepEndBy p sep
; return (x:xs)
}
<|> return [x]
}
 */
fun <T, R, S> sepEndBy1(parser: Parser<T, R>, sep: Parser<T, S>): Parser<T, List<R>> = { tokens: Sequence<T> ->
    // Kotlin compiler isn't handling intertwined lambdas nicely; it needs explicit type information:
    fun combine(x: R, xs: List<R>): Parser<T, List<R>> {
        return give(listOf(x) + xs)
    }
    fun help(x: R): Parser<T, List<R>> {
        return sep right sepEndBy(parser, sep) bind { xs: List<R> -> combine(x, xs) }
    }
    (parser bind {x: R -> help(x) or give(listOf(x))})(tokens)
}

/**
 * Parses `parser` zero or many times, separated and ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepEndBy(parser: Parser<T, R>, sep: Parser<T, S>): Parser<T, List<R>> = sepEndBy1(parser, sep) or give(emptyList())


infix fun <T, F, S> Parser<T, F>.and(parser: Parser<T, S>): Parser<T, Pair<F, S>> = { tokens: Sequence<T> ->
    val firstParsed = this(tokens)
    val (fr, fts) = firstParsed
    when (fr) {
        is Either.Left -> fr to firstParsed.second
        is Either.Right -> {
            val secondParsed = parser(fts)
            val (sr, sts) = secondParsed
            when (sr) {
                is Either.Left -> sr to secondParsed.second
                is Either.Right -> {
                    Either.right(fr.value to sr.value) to sts
                }
            }
        }
    }
}


infix fun <T, F, S> Parser<T, F>.bind(parser: (F) -> Parser<T, S>): Parser<T, S> = { tokens: Sequence<T> ->
    val (r, ts) = this(tokens)
    when(r) {
        is Either.Left -> r to ts
        is Either.Right -> parser(r.value)(ts)
    }
}

//fun <T, F, S> bind(base: Parser<T, F>, parser: (F) -> Parser<T, S>): Parser<T, S> = { tokens: Sequence<T> ->
//    val (r, ts) = base(tokens)
//    when (r) {
//        is Either.Left -> r to ts
//        is Either.Right -> parser(r.value)(ts)
//    }
//}

infix fun <T, F, S> Parser<T, F>.left(parser: Parser<T, S>): Parser<T, F> = { tokens: Sequence<T> ->
    (this and parser)(tokens).let {
        it.mapResult { it.first }
    }
}

infix fun <T, F, S> Parser<T, F>.right(parser: Parser<T, S>): Parser<T, S> = { tokens: Sequence<T> ->
    (this and parser)(tokens).let {
        it.mapResult { it.second }
    }
}

infix fun <T, F, S> Parser<T, F>.either(parser: Parser<T, S>) = { tokens: Sequence<T> ->
    val (fr, fts) = this(tokens)
    when (fr) {
        is Either.Left -> {
            if (fts == tokens) {
                val (sr, sts) = parser(tokens)
                when (sr) {
                    is Either.Left -> Either.left(fr.value + sr.value) to tokens
                    is Either.Right -> Either.right(sr) to sts
                }
            } else {
                fr to fts
            }
        }
        is Either.Right -> Either.right(Either.left(fr.value)) to fts
    }
}
