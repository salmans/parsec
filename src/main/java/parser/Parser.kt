package parser

import tools.Either

const val END_OF_SOURCE = ""

open class ParserException(message: String?) : Exception(message) {
    open fun combine(exception: ParserException): ParserException {
        return ParserException("${this.message}\n${exception.message}")
    }
}

class UnexpectedTokenException : ParserException {
    private val tokens: List<String>
    private val found: String

    constructor(vararg tokens: String, found: String) : super(null) {
        this.tokens = tokens.toList()
        this.found = found
    }

    constructor(tokens: List<String>, found: String) : super(null) {
        this.tokens = tokens
        this.found = found
    }

    override val message: String?
        get() = if (tokens.size == 1) {
            "Expecting '${tokens[0]}' but '$found' was found."
        } else {
            "Expecting one of ${tokens.joinToString(",") { "'$it'" }} but '$found' was found."
        }

    override fun combine(exception: ParserException) = when (exception) {
        is UnexpectedTokenException -> UnexpectedTokenException(this.tokens + exception.tokens, this.found)
        else -> super.combine(exception)
    }
}

operator fun ParserException.plus(exception: ParserException) = this.combine(exception)

typealias ParserResult<T, R> = Either<ParserException, Pair<R, Sequence<T>>>

typealias Parser<T, R> = (tokens: Sequence<T>) -> ParserResult<T, R>

fun <T> token(token: T) = { tokens: Sequence<T> ->
    if (tokens.firstOrNull() == token)
        Either.right(token to tokens.drop(1))
    else {
        val found = if (tokens.firstOrNull() != null) tokens.firstOrNull().toString() else END_OF_SOURCE
        Either.left(UnexpectedTokenException(token.toString(), found = found))
    }
}