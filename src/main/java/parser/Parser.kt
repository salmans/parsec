package parser

import tools.Either

open class ParserException(open var message: String?) {
    open fun combine(exception: ParserException): ParserException {
        return this
    }
}

class UnexpectedTokenException<T> : ParserException {
    val tokens: List<T>
    val found: T

    constructor(vararg tokens: T, found: T) : super(null) {
        this.tokens = tokens.toList()
        this.found = found
    }

    constructor(tokens: List<T>, found: T) : super(null) {
        this.tokens = tokens
        this.found = found
    }

    override var message: String? = null
        get() = if (tokens.size == 1) {
            "Expecting '${tokens[0]}' but '$found' was found."
        } else {
            "Expecting one of ${tokens.joinToString(",") { "'$it'" }} but '$found' was found."
        }

    override fun combine(exception: ParserException) = when (exception) {
        is UnexpectedTokenException<*> -> UnexpectedTokenException((this.tokens + exception.tokens).distinct(), this.found)
        else -> super.combine(exception)
    }
}

class UnexpectedEndOfInputException<T>: ParserException {
    val tokens: List<T>

    constructor(vararg tokens: T) : super(null) {
        this.tokens = tokens.toList()
    }

    constructor(tokens: List<T>) : super(null) {
        this.tokens = tokens
    }

    override var message: String? = null
        get() = when(tokens.size) {
            0 -> "Unexpected end of input was found."
            1 -> "Expecting '${tokens[0]}' but end of input was found."
            else -> "Expecting one of ${tokens.joinToString(",") { "'$it'" }} but end of input was found."
        }

    override fun combine(exception: ParserException) = when (exception) {
        is UnexpectedEndOfInputException<*> -> UnexpectedEndOfInputException((this.tokens + exception.tokens).distinct())
        else -> super.combine(exception)
    }
}

class UnexpectedException(unexpected: String) : ParserException("Unexpected '$unexpected' was found.")

operator fun ParserException.plus(exception: ParserException) = this.combine(exception)

typealias ParserResult<T, R> = Pair<Either<ParserException, R>, Sequence<T>>

typealias Parser<T, R> = (tokens: Sequence<T>) -> ParserResult<T, R>