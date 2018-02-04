package combinator

import org.junit.Assert.assertEquals
import org.junit.Test
import parser.Parser
import tools.Either

class CombinatorTest {
    @Test
    fun anyToken() {
        assertEquals('A' to "".toList(), success("A".asSequence(), anyToken<Char>()))
        assertEquals('C' to "BA".toList(), success("CBA".asSequence(), anyToken<Char>()))
        assertEquals("Unexpected end of input was found.", failure("".asSequence(), anyToken<Char>()))
    }

    @Test
    fun lookAhead() {
        assertEquals('A' to "AB".toList(), success("AB".asSequence(), lookAhead(charA)))
        assertEquals(('A' to 'B') to "AB".toList(), success("AB".asSequence(), lookAhead(charA and charB)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), lookAhead(charA)))
        assertEquals("Expecting 'B' but end of input was found.", failure("A".asSequence(), lookAhead(charA and charB)))
    }

    @Test
    fun attempt() {
        assertEquals('A' to "B".toList(), success("AB".asSequence(), attempt(charA)))
        assertEquals(('A' to 'B') to emptyList<Char>(), success("AB".asSequence(), attempt(charA and charB)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), attempt(charA)))
        assertEquals("Expecting 'B' but end of input was found.", failure("A".asSequence(), attempt(charA and charB)))
    }

    @Test
    fun notFollowedBy() {
        assertEquals(Unit to "B".toList(), success("B".asSequence(), notFollowedBy(charA)))
        assertEquals("Unexpected 'A' was found.", failure("A".asSequence(), notFollowedBy(charA)))
    }

    @Test
    fun eof() {
        assertEquals(Unit to emptyList<Char>(), success("".asSequence(), eof<Char>()))
        assertEquals("Unexpected 'A' was found.", failure("A".asSequence(), eof<Char>()))
    }

    @Test
    fun many() {
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), many(charA)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("AABC".asSequence(), many(charA)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("AAAAABC".asSequence(), many(charA)))
        assertEquals(emptyList<Char>() to "BC".toList(), success("BC".asSequence(), many(charA)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABAC".asSequence(), many(charA and charB)))
    }

    @Test
    fun many1() {
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), many1(charA)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("AABC".asSequence(), many1(charA)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("AAAAABC".asSequence(), many1(charA)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), many1(charA)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABAC".asSequence(), many1(charA and charB)))
    }

    @Test
    fun manyTill() {
        assertEquals(listOf('A') to emptyList<Char>(), success("AB".asSequence(), manyTill(charA, charB)))
        assertEquals(listOf('A', 'A') to "C".toList(), success("AABC".asSequence(), manyTill(charA, charB)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to emptyList<Char>(), success("AAAAABC".asSequence(), manyTill(charA, charB and charC)))
        assertEquals(listOf('B', 'B') to emptyList<Char>(), success("ABABC".asSequence(), manyTill(charA right charB, charC)))
        assertEquals("Expecting one of 'C','B' but 'A' was found.", failure("ABAC".asSequence(), manyTill(charA and charB, charC)))
        assertEquals("Expecting one of 'C','A' but 'A' was found.", failure("AAAAABC".asSequence(), manyTill(charA, charC)))
        assertEquals("Expecting 'B' but 'A' was found.", failure("AAAAA".asSequence(), manyTill(charA, charB)))
    }

    @Test
    fun skipMany() {
        assertEquals(Unit to listOf('B'), success("AB".asSequence(), skipMany(charA)))
        assertEquals(Unit to "BC".toList(), success("AABC".asSequence(), skipMany(charA)))
        assertEquals(Unit to "BC".toList(), success("AAAAABC".asSequence(), skipMany(charA)))
        assertEquals(Unit to "BC".toList(), success("BC".asSequence(), skipMany(charA)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABAC".asSequence(), skipMany(charA and charB)))
    }

    @Test
    fun skipMany1() {
        assertEquals(Unit to listOf('B'), success("AB".asSequence(), skipMany1(charA)))
        assertEquals(Unit to "BC".toList(), success("AABC".asSequence(), skipMany1(charA)))
        assertEquals(Unit to "BC".toList(), success("AAAAABC".asSequence(), skipMany1(charA)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), skipMany1(charA)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABAC".asSequence(), skipMany1(charA and charB)))
    }

    @Test
    fun choice() {
        assertEquals('A' to listOf('B'), success("AB".asSequence(), choice(listOf(charA))))
        assertEquals('A' to listOf('B'), success("AB".asSequence(), choice(listOf(charA, charB))))
        assertEquals("Expecting one of 'A','B' but 'C' was found.", failure("C".asSequence(), choice(listOf(charA, charB))))
        assertEquals("Expecting 'A' but 'B' was found.", failure("B".asSequence(), choice(listOf(charA))))
        assertEquals("Cannot apply the `choice` an empty list of parsers.", failure("ABC".asSequence(), choice(emptyList<Parser<Char, Char>>())))
    }

    @Test
    fun count() {
        assertEquals(listOf<Char>() to "AB".toList(), success("AB".asSequence(), count(0, charA)))
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), count(1, charA)))
        assertEquals(listOf('A', 'A', 'A') to listOf('B'), success("AAAB".asSequence(), count(3, charA)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("B".asSequence(), count(1, charA)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("AB".asSequence(), count(2, charA)))
    }

    @Test
    fun between() {
        assertEquals('B' to emptyList<Char>(), success("ABA".asSequence(), between(charA, charA, charB)))
        assertEquals('B' to listOf('C'), success("ABAC".asSequence(), between(charA, charA, charB)))
        assertEquals("Expecting 'A' but 'C' was found.", failure("ABC".asSequence(), between(charA, charA, charB)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BAC".asSequence(), between(charA, charA, charB)))
        assertEquals("Expecting 'B' but 'A' was found.", failure("AA".asSequence(), between(charA, charA, charB)))
    }

    @Test
    fun and() {
        assertEquals(('A' to 'B') to emptyList<Char>(), success("AB".asSequence(), charA and charB))
        assertEquals(('A' to 'B') to listOf('C'), success("ABC".asSequence(), charA and charB))
        assertEquals((('A' to 'B') to 'C') to emptyList<Char>(), success("ABC".asSequence(), charA and charB and charC))
        assertEquals("Expecting 'B' but end of input was found.", failure("A".asSequence(), charA and charB))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), charA and charB))
        assertEquals("Expecting 'B' but 'C' was found.", failure("AC".asSequence(), charA and charB))
    }

    @Test
    fun left() {
        assertEquals('A' to emptyList<Char>(), success("AB".asSequence(), charA left charB))
        assertEquals('A' to listOf('C'), success("ABC".asSequence(), charA left charB))
        assertEquals('A' to emptyList<Char>(), success("ABC".asSequence(), charA left charB left charC))
        assertEquals("Expecting 'B' but end of input was found.", failure("A".asSequence(), charA left charB))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), charA left charB))
        assertEquals("Expecting 'B' but 'C' was found.", failure("AC".asSequence(), charA left charB))
    }

    @Test
    fun right() {
        assertEquals('B' to emptyList<Char>(), success("AB".asSequence(), charA right charB))
        assertEquals('B' to listOf('C'), success("ABC".asSequence(), charA right charB))
        assertEquals('C' to emptyList<Char>(), success("ABC".asSequence(), charA right charB right charC))
        assertEquals("Expecting 'B' but end of input was found.", failure("A".asSequence(), charA right charB))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), charA right charB))
        assertEquals("Expecting 'B' but 'C' was found.", failure("AC".asSequence(), charA right charB))
    }

    @Test
    fun optionMaybe() {
        assertEquals('A' to listOf('B'), success("AB".asSequence(), optionMaybe(charA)))
        assertEquals(null to listOf('B'), success("B".asSequence(), optionMaybe(charA)))
        assertEquals('A' to "BC".toList(), success("ABC".asSequence(), optionMaybe(optionMaybe(charA))))
        assertEquals(null to "ABC".toList(), success("ABC".asSequence(), optionMaybe(optionMaybe(charB))))
        assertEquals("Expecting 'A' but 'B' was found.", failure("ABC".asSequence(), optional(charA and charA)))
    }

    @Test
    fun optional() {
        assertEquals(Unit to listOf('B'), success("AB".asSequence(), optional(charA)))
        assertEquals(Unit to listOf('B'), success("B".asSequence(), optional(charA)))
        assertEquals(Unit to "BC".toList(), success("ABC".asSequence(), optional(optional(charA))))
        assertEquals(Unit to "ABC".toList(), success("ABC".asSequence(), optional(optional(charB))))
        assertEquals("Expecting 'A' but 'B' was found.", failure("ABC".asSequence(), optional(charA and charA)))
    }

    @Test
    fun option() {
        assertEquals('A' to listOf('B'), success("AB".asSequence(), option('X', charA)))
        assertEquals('X' to listOf('B'), success("B".asSequence(), option('X', charA)))
        assertEquals('A' to "BC".toList(), success("ABC".asSequence(), option('Y', option('X', charA))))
        assertEquals('X' to "ABC".toList(), success("ABC".asSequence(), option('Y', option('X', charB))))
        assertEquals("Expecting 'A' but 'B' was found.", failure("ABC".asSequence(), option('X', charA and charA)))
    }

    @Test
    fun sepBy1() {
        assertEquals(listOf('A') to emptyList<Char>(), success("A".asSequence(), sepBy1(charA, charC)))
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), sepBy1(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACABC".asSequence(), sepBy1(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACABC".asSequence(), sepBy1(charA, charC)))
        assertEquals(listOf('A' to 'B') to "AC".toList(), success("ABAC".asSequence(), sepBy1(charA and charB, charC)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), sepBy1(charA, charC)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("ACB".asSequence(), sepBy1(charA, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), sepBy1(charA, charB and charC)))
    }

    @Test
    fun sepBy() {
        assertEquals(listOf('A') to emptyList<Char>(), success("A".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACABC".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACABC".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A' to 'B') to "AC".toList(), success("ABAC".asSequence(), sepBy(charA and charB, charC)))
        assertEquals(emptyList<Char>() to "BC".toList(), success("BC".asSequence(), sepBy(charA, charC)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("ACB".asSequence(), sepBy(charA, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), sepBy(charA, charB and charC)))
    }

    @Test
    fun endBy1() {
        assertEquals(listOf('A') to listOf('B'), success("ACB".asSequence(), endBy1(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACACBC".asSequence(), endBy1(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACACBC".asSequence(), endBy1(charA, charC)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABCAC".asSequence(), endBy1(charA and charB, charC)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), endBy1(charA, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), endBy1(charA, charB and charC)))
    }

    @Test
    fun sepEndBy1() {
        assertEquals(listOf('A') to listOf('B'), success("ACB".asSequence(), sepEndBy1(charA, charC)))
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), sepEndBy1(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACACBC".asSequence(), sepEndBy1(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACABC".asSequence(), sepEndBy1(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACACBC".asSequence(), sepEndBy1(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACABC".asSequence(), sepEndBy1(charA, charC)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABCAC".asSequence(), sepEndBy1(charA and charB, charC)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), sepEndBy1(charA, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), sepEndBy1(charA, charB and charC)))
    }

    @Test
    fun sepEndBy() {
        assertEquals(listOf('A') to listOf('B'), success("ACB".asSequence(), sepEndBy(charA, charC)))
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), sepEndBy(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACACBC".asSequence(), sepEndBy(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACABC".asSequence(), sepEndBy(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACACBC".asSequence(), sepEndBy(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACABC".asSequence(), sepEndBy(charA, charC)))
        assertEquals(emptyList<Char>() to "BC".toList(), success("BC".asSequence(), sepEndBy(charA, charC)))
        assertEquals(emptyList<Char>() to "CBC".toList(), success("CBC".asSequence(), sepEndBy(charA, charC)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABCAC".asSequence(), sepEndBy(charA and charB, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), sepEndBy(charA, charB and charC)))
    }

    @Test
    fun endBy() {
        assertEquals(listOf('A') to listOf('B'), success("ACB".asSequence(), endBy(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACACBC".asSequence(), endBy(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACACBC".asSequence(), endBy(charA, charC)))
        assertEquals(emptyList<Char>() to "BC".toList(), success("BC".asSequence(), endBy(charA, charC)))
        assertEquals(emptyList<Char>() to "CBC".toList(), success("CBC".asSequence(), endBy(charA, charC)))
        assertEquals("Expecting 'B' but 'C' was found.", failure("ABCAC".asSequence(), endBy(charA and charB, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), endBy(charA, charB and charC)))
    }

    @Test
    fun chainl1() {
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), chainl1(charA, give({ _, x -> x }))))
        assertEquals('B' to listOf('C'), success("ABC".asSequence(), chainl1(charA or charB, give({ _, x -> x }))))
        assertEquals('C' to emptyList<Char>(), success("ABC".asSequence(), chainl1(charA or charB or charC, give({ _, x -> x }))))
        assertEquals('b' to listOf('C'), success("BAAAABC".asSequence(), chainl1(charA or charB, give({ x, _ -> x.toLowerCase() }))))
        assertEquals(3 to emptyList<Char>(), success("0+1+2".asSequence(), chainl1(pNum(), pOp())))
        assertEquals(-7 to emptyList<Char>(), success("0+1-2+3-9".asSequence(), chainl1(pNum(), pOp())))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BA".asSequence(), chainl1(charA, give({ _, x -> x }))))
    }

    @Test
    fun chainl() {
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), chainl(charA, give({ _, x -> x }), 'X')))
        assertEquals('B' to listOf('C'), success("ABC".asSequence(), chainl(charA or charB, give({ _, x -> x }), 'X')))
        assertEquals('C' to emptyList<Char>(), success("ABC".asSequence(), chainl(charA or charB or charC, give({ _, x -> x }), 'X')))
        assertEquals('b' to listOf('C'), success("BAAAABC".asSequence(), chainl(charA or charB, give({ x, _ -> x.toLowerCase() }), 'X')))
        assertEquals(3 to emptyList<Char>(), success("0+1+2".asSequence(), chainl(pNum(), pOp(), 0)))
        assertEquals(-7 to emptyList<Char>(), success("0+1-2+3-9".asSequence(), chainl(pNum(), pOp(), 0)))
        assertEquals('X' to "BA".toList(), success("BA".asSequence(), chainl(charA, give({ _, x -> x }), 'X')))
    }

    @Test
    fun chainr1() {
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), chainr1(charA, give({ _, x -> x }))))
        assertEquals('B' to listOf('C'), success("ABC".asSequence(), chainr1(charA or charB, give({ _, x -> x }))))
        assertEquals('C' to emptyList<Char>(), success("ABC".asSequence(), chainr1(charA or charB or charC, give({ _, x -> x }))))
        assertEquals('b' to listOf('C'), success("BAAAABC".asSequence(), chainr1(charA or charB, give({ x, _ -> x.toLowerCase() }))))
        assertEquals(3 to emptyList<Char>(), success("0+1+2".asSequence(), chainr1(pNum(), pOp())))
        assertEquals(5 to emptyList<Char>(), success("0+1-2+3-9".asSequence(), chainr1(pNum(), pOp())))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BA".asSequence(), chainr1(charA, give({ _, x -> x }))))
    }

    @Test
    fun chainr() {
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), chainr(charA, give({ _, x -> x }), 'X')))
        assertEquals('B' to listOf('C'), success("ABC".asSequence(), chainr(charA or charB, give({ _, x -> x }), 'X')))
        assertEquals('C' to emptyList<Char>(), success("ABC".asSequence(), chainr(charA or charB or charC, give({ _, x -> x }), 'X')))
        assertEquals('b' to listOf('C'), success("BAAAABC".asSequence(), chainr(charA or charB, give({ x, _ -> x.toLowerCase() }), 'X')))
        assertEquals(3 to emptyList<Char>(), success("0+1+2".asSequence(), chainr(pNum(), pOp(), 0)))
        assertEquals(5 to emptyList<Char>(), success("0+1-2+3-9".asSequence(), chainr(pNum(), pOp(), 0)))
        assertEquals('X' to "BA".toList(), success("BA".asSequence(), chainr(charA, give({ _, x -> x }), 'X')))
    }


    @Test
    fun bind() {
        run {
            fun tlc(ch: Char): Parser<Char, Char> {
                return give(ch.toLowerCase())
            }
            assertEquals('a' to "BC".toList(), success("ABC".asSequence(),
                    charA bind { x: Char -> tlc(x) }))
        }
        run {
            fun tlc(ch: Char): Parser<Char, Char> {
                return give(ch.toLowerCase())
            }
            assertEquals("Expecting 'C' but 'B' was found.", failure("ABC".asSequence(),
                    (charA left charC) bind { x: Char -> tlc(x) }))
        }
        run {
            fun tlc(ch: Char): Parser<Char, Char> {
                return charA right give(ch.toLowerCase())
            }
            assertEquals("Expecting 'A' but 'B' was found.", failure("ACBC".asSequence(),
                    (charA left charC) bind { x: Char -> tlc(x) }))
        }
        run {
            fun tlc(ch: Char): Parser<Char, Char> {
                return charA right give(ch.toLowerCase())
            }
            assertEquals("Expecting 'A' but 'B' was found.", failure("ABC".asSequence(),
                    charA bind { x: Char -> tlc(x) }))
        }
    }

    @Test
    fun or() {
        assertEquals('A' to listOf('B'), success("AB".asSequence(), charA or charB))
        assertEquals('B' to listOf('C'), success("BC".asSequence(), charA or charB))
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), charA or charB))
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), charB or charA))
        assertEquals('C' to emptyList<Char>(), success("C".asSequence(), charA or charB or charC))
        assertEquals("Expecting one of 'A','B' but end of input was found.", failure("".asSequence(), charA or charB))
        assertEquals("Expecting 'C' but 'B' was found.", failure("AB".asSequence(), (charA and charC) or charB))
        assertEquals("Expecting one of 'A','B' but 'C' was found.", failure("C".asSequence(), charA or charB))
    }

    @Test
    fun either() {
        assertEquals(Either.left('A') to listOf('B'), success("AB".asSequence(), charA either charB))
        assertEquals(Either.right('B') to listOf('C'), success("BC".asSequence(), charA either charB))
        assertEquals(Either.left('A') to emptyList<Char>(), success("A".asSequence(), charA either charB))
        assertEquals(Either.right('A') to emptyList<Char>(), success("A".asSequence(), charB either charA))
        assertEquals(Either.right('C') to emptyList<Char>(), success("C".asSequence(), charA either charB either charC))
        assertEquals("Expecting one of 'A','B' but end of input was found.", failure("".asSequence(), charA either charB))
        assertEquals("Expecting 'C' but 'B' was found.", failure("AB".asSequence(), (charA and charC) either charB))
        assertEquals("Expecting one of 'A','B' but 'C' was found.", failure("C".asSequence(), charA either charB))
    }
}
