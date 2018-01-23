package combinator

import parser.Parser
import parser.ParserResult
import tools.Either

class ManyCombinator<T, out R>(private val parser: Parser<T, R>) : Parser<T, List<R>>() {
    override fun parse(tokens: Sequence<T>): ParserResult<T, List<R>> {
        var result = emptyList<R>()
        var toks = tokens

        while (true) {
            val parsed = parser.parse(toks)
            if (parsed is Either.Left) {
                break
            } else if (parsed is Either.Right){
                val (r, ts) = parsed.value
                toks = ts
                result += r
            }
        }

        return Either.right(result to toks)
    }
}

fun <T, R> many(parser: () -> Parser<T, R>) = ManyCombinator(parser())