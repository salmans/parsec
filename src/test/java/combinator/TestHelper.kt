package combinator

import parser.Parser
import parser.token
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

fun <T, R> success(tokens: Sequence<T>, parser: Parser<T, R>) = parser(tokens).let {
    (it.first as Either.Right).value to it.second.toList()
}
fun <T, R> failure(tokens: Sequence<T>, parser: Parser<T, R>) = parser(tokens).first.left()!!.message