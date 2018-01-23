package tools

fun <T> identity(value: T) = value

infix fun <V, T, R> Function1<T, R>.compose(function: (V) -> T): (V) -> R = { v: V -> this(function(v)) }