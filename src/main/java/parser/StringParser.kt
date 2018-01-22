package parser

import tools.Either

class CharParser(private val character: Char) : Parser<Char, Char>() {
    override fun parse(tokens: Sequence<Char>): Either<ParserException, Pair<Char, Sequence<Char>>> {
        return if (tokens.firstOrNull() == character)
            Either.right(character to tokens.drop(1))
        else {
            val found = if (tokens.firstOrNull() != null) tokens.firstOrNull().toString() else END_OF_SOURCE
            Either.left(UnexpectedTokenException(character.toString(), found = found))
        }
    }
}

fun pChar(character: Char) = CharParser(character)