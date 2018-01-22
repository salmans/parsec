package combinator

import parser.Parser
import parser.ParserResult
import tools.Either

class AndCombinator<T, out F, out S>(private val left: Parser<T, F>, private val right: Parser<T, S>) : Parser<T, Pair<F, S>>() {
    override fun parse(tokens: Sequence<T>): ParserResult<T, Pair<F, S>> {
        val firstResult = left.parse(tokens)
        return when (firstResult) {
            is Either.Left -> firstResult
            is Either.Right -> {
                val (fr, fts) = firstResult.value
                val secondResult = right.parse(fts)
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
}

class LeftCombinator<T, out F, out S>(private val left: Parser<T, F>, private val right: Parser<T, S>) : Parser<T, F>() {
    override fun parse(tokens: Sequence<T>): ParserResult<T, F> {
        val firstResult = left.parse(tokens)
        return when (firstResult) {
            is Either.Left -> firstResult
            is Either.Right -> {
                val (fr, fts) = firstResult.value
                val secondResult = right.parse(fts)
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
}

class RightCombinator<T, out F, out S>(private val left: Parser<T, F>, private val right: Parser<T, S>) : Parser<T, S>() {
    override fun parse(tokens: Sequence<T>): ParserResult<T, S> {
        val firstResult = left.parse(tokens)
        return when (firstResult) {
            is Either.Left -> firstResult
            is Either.Right -> {
                val (_, fts) = firstResult.value
                return right.parse(fts)
            }
        }
    }
}

infix fun <T, F, S> Parser<T, F>.and(parser: Parser<T, S>) = AndCombinator(this, parser)
infix fun <T, F, S> Parser<T, F>.left(parser: Parser<T, S>) = LeftCombinator(this, parser)
infix fun <T, F, S> Parser<T, F>.right(parser: Parser<T, S>) = RightCombinator(this, parser)