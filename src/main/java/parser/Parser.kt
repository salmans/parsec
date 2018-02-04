package parser

import tools.Either

const val END_OF_SOURCE = ""

open class ParserException(message: String?) : Exception(message) {
    open fun combine(exception: ParserException): ParserException {
        return this
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
        is UnexpectedTokenException -> UnexpectedTokenException((this.tokens + exception.tokens).distinct(), this.found)
        else -> super.combine(exception)
    }
}

class UnexpectedEndOfInputException: ParserException {
    private val tokens: List<String>

    constructor(vararg tokens: String) : super(null) {
        this.tokens = tokens.toList()
    }

    constructor(tokens: List<String>) : super(null) {
        this.tokens = tokens
    }

    override val message: String?
        get() = when(tokens.size) {
            0 -> "Unexpected end of input was found."
            1 -> "Expecting '${tokens[0]}' but end of input was found."
            else -> "Expecting one of ${tokens.joinToString(",") { "'$it'" }} but end of input was found."
        }

    override fun combine(exception: ParserException) = when (exception) {
        is UnexpectedEndOfInputException -> UnexpectedEndOfInputException((this.tokens + exception.tokens).distinct())
        else -> super.combine(exception)
    }
}

class UnexpectedException(unexpected: String) : ParserException("Unexpected '$unexpected' was found.")

operator fun ParserException.plus(exception: ParserException) = this.combine(exception)

typealias ParserResult<T, R> = Pair<Either<ParserException, R>, Sequence<T>>

fun <T, R, S> ParserResult<T, R>.mapResult(transform: (R) -> S): ParserResult<T, S> {
    val parsedResult = this.first
    return when (parsedResult) {
        is Either.Left -> Either.Left(parsedResult.value) to this.second
        is Either.Right -> Either.Right(transform(parsedResult.value)) to this.second
    }
}

typealias Parser<T, R> = (tokens: Sequence<T>) -> ParserResult<T, R>