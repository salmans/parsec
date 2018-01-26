package tools

sealed class Either<out A, out B> private constructor() {
    data class Left<out A>(val value: A) : Either<A, Nothing>()
    data class Right<out B>(val value: B) : Either<Nothing, B>()

    companion object {
        fun <A> left(value: A): Either<A, Nothing> = Left(value)
        fun <B> right(value: B): Either<Nothing, B> = Right(value)
    }

    fun isLeft(): Boolean = when (this) {
        is Left<A> -> true
        else -> false
    }

    fun isRight(): Boolean = when (this) {
        is Right<B> -> true
        else -> false
    }

    fun left(): A? = when (this) {
        is Left<A> -> value
        else -> null
    }

    fun right(): B? = when (this) {
        is Right<B> -> value
        else -> null
    }

    fun <C> either(left: (A) -> C, right: (B) -> C): C = when (this) {
        is Either.Left -> left(value)
        is Either.Right -> right(value)
    }
}