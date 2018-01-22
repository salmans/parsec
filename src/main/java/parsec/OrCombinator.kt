package parsec

import tools.Either

class OrCombinator<T, out F, out S>(private val left: Parser<T, F>, private val right: Parser<T, S>) : Parser<T, Either<F, S>>() {
    override fun parse(tokens: Sequence<T>): ParserResult<T, Either<F, S>> {
        val firstResult = left.parse(tokens)
        when (firstResult) {
            is Either.Left -> {
                val secondResult = right.parse(tokens)
                return when(secondResult) {
                    is Either.Left -> Either.left(firstResult.value + secondResult.value)
                    is Either.Right -> {
                        val (sr, sts) = secondResult.value
                        return Either.right(Either.right(sr) to sts)
                    }
                }

            }
            is Either.Right -> {
                val (fr, fts) = firstResult.value
                return Either.right(Either.left(fr) to fts)
            }
        }
    }
}

infix fun <T, F, S> Parser<T, F>.or(parser: Parser<T, S>) = OrCombinator(this, parser)