package tools

infix fun <V, T, R> Function1<T, R>.compose(function: (V) -> T): (V) -> R = { v: V -> this(function(v)) }