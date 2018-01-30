package combinator

import org.junit.Assert.assertEquals
import org.junit.Test
import parser.Parser
import tools.Either

class CombinatorTest {
    @Test
    fun lookAhead() {
        assertEquals('A' to "AB".toList(), success("AB".asSequence(), lookAhead(charA)))
        assertEquals(('A' to 'B') to "AB".toList(), success("AB".asSequence(), lookAhead(charA and charB)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), lookAhead(charA)))
        assertEquals("Expecting 'B' but '' was found.", failure("A".asSequence(), lookAhead(charA and charB)))
    }

    @Test
    fun attempt() {
        assertEquals('A' to "B".toList(), success("AB".asSequence(), attempt(charA)))
        assertEquals(('A' to 'B') to emptyList<Char>(), success("AB".asSequence(), attempt(charA and charB)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), attempt(charA)))
        assertEquals("Expecting 'B' but '' was found.", failure("A".asSequence(), attempt(charA and charB)))
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
        assertEquals(('A' to 'B') to emptyList<Char>(), success("AB".asSequence(),charA and charB))
        assertEquals(('A' to 'B') to listOf('C'), success("ABC".asSequence(), charA and charB))
        assertEquals((('A' to 'B') to 'C') to emptyList<Char>(), success("ABC".asSequence(), charA and charB and charC))
        assertEquals("Expecting 'B' but '' was found.", failure("A".asSequence(), charA and charB))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), charA and charB))
        assertEquals("Expecting 'B' but 'C' was found.", failure("AC".asSequence(), charA and charB))
    }

    @Test
    fun left() {
        assertEquals('A' to emptyList<Char>(), success("AB".asSequence(),charA left charB))
        assertEquals('A' to listOf('C'), success("ABC".asSequence(),charA left charB))
        assertEquals('A' to emptyList<Char>(), success("ABC".asSequence(),charA left charB left charC))
        assertEquals("Expecting 'B' but '' was found.", failure("A".asSequence(), charA left charB))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(), charA left charB))
        assertEquals("Expecting 'B' but 'C' was found.", failure("AC".asSequence(), charA left charB))
    }

    @Test
    fun right() {
        assertEquals('B' to emptyList<Char>(), success("AB".asSequence(), charA right charB))
        assertEquals('B' to listOf('C'), success("ABC".asSequence(), charA right charB))
        assertEquals('C' to emptyList<Char>(), success("ABC".asSequence(), charA right charB right charC))
        assertEquals("Expecting 'B' but '' was found.", failure("A".asSequence(),charA right charB))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BC".asSequence(),charA right charB))
        assertEquals("Expecting 'B' but 'C' was found.", failure("AC".asSequence(),charA right charB))
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
        assertEquals(listOf('A') to listOf('B'), success("AB".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A', 'A') to "BC".toList(), success("ACABC".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A', 'A', 'A', 'A', 'A') to "BC".toList(), success("ACACACACABC".asSequence(), sepBy(charA, charC)))
        assertEquals(listOf('A' to 'B') to "AC".toList(), success("ABAC".asSequence(), sepBy(charA and charB, charC)))
        assertEquals(emptyList<Char>() to "BC".toList(), success("BC".asSequence(), sepBy(charA, charC)))
        assertEquals("Expecting 'A' but 'B' was found.", failure("ACB".asSequence(), sepBy(charA, charC)))
        assertEquals("Expecting 'C' but 'A' was found.", failure("ABAC".asSequence(), sepBy(charA, charB and charC)))
    }

    @Test
    fun or() {
        assertEquals('A' to listOf('B'), success("AB".asSequence(),charA or charB))
        assertEquals('B' to listOf('C'), success("BC".asSequence(),charA or charB))
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(), charA or charB))
        assertEquals('A' to emptyList<Char>(), success("A".asSequence(),charB or charA))
        assertEquals('C' to emptyList<Char>(), success("C".asSequence(),charA or charB or charC))
        assertEquals("Expecting one of 'A','B' but '' was found.", failure("".asSequence(), charA or charB))
        assertEquals("Expecting 'C' but 'B' was found.", failure("AB".asSequence(), (charA and charC) or charB))
        assertEquals("Expecting one of 'A','B' but 'C' was found.", failure("C".asSequence(), charA or charB))
    }

    @Test
    fun either() {
        assertEquals(Either.left('A') to listOf('B'), success("AB".asSequence(),charA either charB))
        assertEquals(Either.right('B') to listOf('C'), success("BC".asSequence(),charA either charB))
        assertEquals(Either.left('A') to emptyList<Char>(), success("A".asSequence(), charA either charB))
        assertEquals(Either.right('A') to emptyList<Char>(), success("A".asSequence(),charB either charA))
        assertEquals(Either.right('C') to emptyList<Char>(), success("C".asSequence(),charA either charB either charC))
        assertEquals("Expecting one of 'A','B' but '' was found.", failure("".asSequence(), charA either charB))
        assertEquals("Expecting 'C' but 'B' was found.", failure("AB".asSequence(), (charA and charC) either charB))
        assertEquals("Expecting one of 'A','B' but 'C' was found.", failure("C".asSequence(), charA either charB))
    }
}
