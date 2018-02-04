package parser

import tools.Either

sealed class ParserFailure {
    open fun combine(failure: ParserFailure): ParserFailure {
        return this
    }
}

class UnexpectedTokenFailure<T> : ParserFailure {
    val tokens: List<T>

    constructor(vararg tokens: T) {
        this.tokens = tokens.toList()
    }

    constructor(tokens: List<T>) {
        this.tokens = tokens
    }

    override fun combine(failure: ParserFailure) = when (failure) {
        is UnexpectedTokenFailure<*> -> UnexpectedTokenFailure((this.tokens + failure.tokens).distinct())
        is UnexpectedEndOfInputFailure<*> -> UnexpectedTokenFailure((this.tokens + failure.tokens).distinct())
        else -> super.combine(failure)
    }
}

class UnexpectedEndOfInputFailure<T>: ParserFailure {
    val tokens: List<T>

    constructor(vararg tokens: T) {
        this.tokens = tokens.toList()
    }

    constructor(tokens: List<T>) {
        this.tokens = tokens
    }

    override fun combine(failure: ParserFailure) = when (failure) {
        is UnexpectedEndOfInputFailure<*> -> UnexpectedEndOfInputFailure((this.tokens + failure.tokens).distinct())
        is UnexpectedTokenFailure<*> -> UnexpectedTokenFailure((this.tokens + failure.tokens).distinct())
        else -> super.combine(failure)
    }
}

class UnexpectedFailure(val message: String) : ParserFailure()

operator fun ParserFailure.plus(failure: ParserFailure) = this.combine(failure)

typealias ParserResult<T, R> = Pair<Either<ParserFailure, R>, Sequence<T>>

typealias Parser<T, R> = (tokens: Sequence<T>) -> ParserResult<T, R>