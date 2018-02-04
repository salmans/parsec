package combinator

import parser.*
import tools.Either


/**
 * Hand in the result without consuming anything (monadic return).
 */
fun <T, R> give(value: R): Parser<T, R> = { tokens: Sequence<T> -> Either.right(value) to tokens }

fun <T, R> parserException(exception: ParserException): Parser<T, R> = { tokens: Sequence<T> -> Either.left(exception) to tokens }

fun <T> token(token: T) = { tokens: Sequence<T> ->
    when (tokens.firstOrNull()) {
        null -> Either.left(UnexpectedEndOfInputException(token)) to tokens
        token -> Either.right(token) to tokens.drop(1)
        else -> Either.left(UnexpectedTokenException(token, found = tokens.firstOrNull().toString())) to tokens
    }
}

/**
 * Accepts and returns any token.
 */
fun <T> anyToken() = { tokens: Sequence<T> ->
    if (tokens.firstOrNull() != null) Either.right(tokens.first()) to tokens.drop(1)
    else Either.left(UnexpectedEndOfInputException<T>()) to tokens
}

/**
 * Fails if `parser` succeeds, without consuming any tokens.
 */
fun <T, R> notFollowedBy(parser: Parser<T, R>) =
        attempt((attempt(parser)) { r ->
            parserException<T, R>(UnexpectedException(r.toString()))
        } or give(Unit))

/**
 * Succeeds at the end of input.
 */
fun <T> eof() = notFollowedBy(anyToken<T>())

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
fun <T, R> many1(parser: Parser<T, R>) = (parser and many(parser)) {
    give<T, List<R>>(listOf(it.first) + it.second)
}


/**
 * Applies `parser` many times until it cannot be applied any more, discarding the result.
 */
fun <T, R> skipMany(parser: Parser<T, R>) = many(parser) right give(Unit)

/**
 * Applies `parser` one or many times until it cannot be applied any more, discarding the result.
 */
fun <T, R> skipMany1(parser: Parser<T, R>) = parser and many(parser) right give(Unit)


/**
 * Applies `parser` zero or many times until `end` succeeds and returns the results of `parser` in a list.
 * For instance, this combinator can be used to parse comments.
 */
fun <T, R, E> manyTill(parser: Parser<T, R>, end: Parser<T, E>) = run {
    fun scan(): Parser<T, List<R>> {
        return (end right give<T, List<R>>(emptyList())) or
                (parser { x ->
                    (scan()) { xs ->
                        give<T, List<R>>(listOf(x) + xs)
                    }
                })
    }

    scan()
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
fun <T, O, C, R> between(open: Parser<T, O>, close: Parser<T, C>, parser: Parser<T, R>) = open right parser left close

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
fun <T, R> optional(parser: Parser<T, R>) = optionMaybe(parser) right give(Unit)


/**
 * Applies `parser` and returns the result of `parser` if it is successful and `default` if `parser` cannot
 * be applied. It fails if `parser` fils after consuming tokens.
 */
fun <T, R> option(default: R, parser: Parser<T, R>) = (optionMaybe(parser)) { give<T, R>(it ?: default) }

/**
 * Parses `parser` one or many times, separated by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepBy1(parser: Parser<T, R>, sep: Parser<T, S>) = (parser and many(sep right parser)) { (first, rest) ->
    give<T, List<R>>(listOf(first) + rest)
}

/**
 * Parses `parser` zero or many times, separated by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepBy(parser: Parser<T, R>, sep: Parser<T, S>) = sepBy1(parser, sep) or give(emptyList())

/**
 * Parses `parser` one or many times, separated and ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> endBy1(parser: Parser<T, R>, sep: Parser<T, S>) = (many1(parser and sep)) {give<T, List<R>>(it.map { it.first })}

/**
 * Parses `parser` zero or many times, separated and ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> endBy(parser: Parser<T, R>, sep: Parser<T, S>) = (many(parser and sep)) {give<T, List<R>>(it.map { it.first })}

/**
 * Parses `parser` one or many times, separated and optionally ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepEndBy1(parser: Parser<T, R>, sep: Parser<T, S>): Parser<T, List<R>> =
    parser { x: R ->
        (sep right sepEndBy(parser, sep)) { xs: List<R> ->
            give<T, List<R>>(listOf(x) + xs)
        } or give(listOf(x))
    }


/**
 * Parses `parser` zero or many times, separated and optionally ended by `sep` and returns the results of parsing `parser` in a list.
 */
fun <T, R, S> sepEndBy(parser: Parser<T, R>, sep: Parser<T, S>): Parser<T, List<R>> = sepEndBy1(parser, sep) or give(emptyList())

/**
 * Applies `parser` one or more time, separated by `operator` and applies left associative `operator` to chain the result.
 * Use this function to eliminate left recursion.
 */
fun <T, R> chainl1(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>): Parser<T, R> = run {
    fun rest(x: R): Parser<T, R> {
        return (operator { f ->
            parser { y ->
                rest(f(x, y))
            }
        }) or give(x)
    }
    parser { x -> rest(x) }
}

/**
 * Applies `parser` zero or more time, separated by `operator` and applies left associative `operator` to chain the result.
 * It returns the default value `default` if `parser` cannot be applied.
 * Use this function to eliminate left recursion.
 */
fun <T, R> chainl(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>, default: R): Parser<T, R> = chainl1(parser, operator) or give(default)

/**
 * Helper for chainr1
 */
private fun <T, R> chainrScan(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>): Parser<T, R> = parser { x -> chainrRest(parser, operator, x) }

/**
 * Helper for chainr1
 */
private fun <T, R> chainrRest(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>, x: R): Parser<T, R> {
    return (operator { f ->
        (chainrScan(parser, operator)) { y ->
            chainrRest(parser, operator, f(x, y))
        }
    }) or give(x)
}

/**
 * Applies `parser` one or more time, separated by `operator` and applies right associative `operator` to chain the result.
 * Use this function to eliminate left recursion.
 */
fun <T, R> chainr1(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>): Parser<T, R> = chainrScan(parser, operator)

/**
 * Applies `parser` zero or more time, separated by `operator` and applies right associative `operator` to chain the result.
 * It returns the default value `default` if `parser` cannot be applied.
 * Use this function to eliminate left recursion.
 */
fun <T, R> chainr(parser: Parser<T, R>, operator: Parser<T, (R, R) -> R>, default: R): Parser<T, R> = chainr1(parser, operator) or give(default)

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

operator fun <T, F, S> Parser<T, F>.invoke(parser: (F) -> Parser<T, S>): Parser<T, S> = { tokens: Sequence<T> ->
    val (r, ts) = this(tokens)
    when (r) {
        is Either.Left -> r to ts
        is Either.Right -> parser(r.value)(ts)
    }
}

infix fun <T, F, S> Parser<T, F>.left(parser: Parser<T, S>): Parser<T, F> = (this and parser) { give<T, F>(it.first) }

infix fun <T, F, S> Parser<T, F>.right(parser: Parser<T, S>): Parser<T, S> = (this and parser) { give<T, S>(it.second) }


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
