package combinator

import parser.Parser
import tools.Either

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