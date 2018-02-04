package combinator

import parser.*
import tools.Either

val charA = token('A')
val charB = token('B')
val charC = token('C')
val charD = token('D')
val charE = token('E')
val charF = token('F')
val charG = token('G')
val charH = token('H')
val charI = token('I')
val charJ = token('J')

fun num0(): Parser<Char, Int> {
    return (token('0') right give(0))
}
fun num1(): Parser<Char, Int> {
    return (token('1') right give(1))
}
fun num2(): Parser<Char, Int> {
    return (token('2') right give(2))
}
fun num3(): Parser<Char, Int> {
    return (token('3') right give(3))
}
fun num4(): Parser<Char, Int> {
    return (token('4') right give(4))
}
fun num5(): Parser<Char, Int> {
    return (token('5') right give(5))
}
fun num6(): Parser<Char, Int> {
    return (token('6') right give(6))
}
fun num7(): Parser<Char, Int> {
    return (token('7') right give(7))
}
fun num8(): Parser<Char, Int> {
    return (token('8') right give(8))
}
fun num9(): Parser<Char, Int> {
    return (token('9') right give(9))
}

fun pNum(): Parser<Char, Int> {
    return num0() or num1() or num2() or num3() or num4() or num5() or num6() or num7() or num8() or num9()
}

fun opPlus(): Parser<Char, (Int, Int) -> Int> {
    return (token('+') right give({ x: Int, y: Int -> x + y }))
}

fun opMinus(): Parser<Char, (Int, Int) -> Int> {
    return (token('-') right give({ x: Int, y: Int -> x - y }))
}

fun pOp(): Parser<Char, (Int, Int) -> Int> {
    return opPlus() or opMinus()
}

fun <T, R> success(tokens: Sequence<T>, parser: Parser<T, R>) = parser(tokens).let {
    (it.first as Either.Right).value to it.second.toList()
}

fun <T, R> failure(tokens: Sequence<T>, parser: Parser<T, R>) = parser(tokens).let {
    val found = it.second.firstOrNull()
    val exception = it.first.left()!!
    when(exception){
        is UnexpectedTokenFailure<*> -> if (exception.tokens.size == 1) {
            "Expecting '${exception.tokens[0]}' but '${found ?: "end of input"}' was found."
        } else {
            "Expecting one of ${exception.tokens.joinToString(",") { "'$it'" }} but '${found ?: "end of input"}' was found."
        }

        is UnexpectedEndOfInputFailure<*> -> when(exception.tokens.size) {
            0 -> "Unexpected 'end of input' was found."
            1 -> "Expecting '${exception.tokens[0]}' but 'end of input' was found."
            else -> "Expecting one of ${exception.tokens.joinToString(",") { "'$it'" }} but 'end of input' was found."
        }
        is UnexpectedFailure -> "Unexpected ${exception.message}"
    }
}