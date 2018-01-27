package combinator

import org.junit.Assert.*
import org.junit.Test
import tools.Either

class AndCombinatorTest {
    @Test
    fun and() {
        assertEquals(('A' to 'B'), (charA and charB)("AB".asSequence()).right()!!.first)
        assertEquals(('A' to 'B'), (charA and charB)("ABC".asSequence()).right()!!.first)
        assertEquals((('A' to 'B') to 'C'), (charA and charB and charC)("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA and charB)("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA and charB)("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA and charB)("AC".asSequence()).left()!!.message)
    }
}

class LeftCombinatorTest {
    @Test
    fun left() {
        assertEquals('A', (charA left charB)("AB".asSequence()).right()!!.first)
        assertEquals('A', (charA left charB)("ABC".asSequence()).right()!!.first)
        assertEquals('A', (charA left charB left charC)("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA left charB)("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA left charB)("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA left charB)("AC".asSequence()).left()!!.message)
    }
}

class RightCombinatorTest {
    @Test
    fun right() {
        assertEquals('B', (charA right charB)("AB".asSequence()).right()!!.first)
        assertEquals('B', (charA right charB)("ABC".asSequence()).right()!!.first)
        assertEquals('C', (charA right charB right charC)("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA right charB)("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA right charB)("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA right charB)("AC".asSequence()).left()!!.message)
    }
}

class ManyCombinatorTest {
    @Test
    fun many() {
        assertEquals(listOf('A'), many(charA)("AB".asSequence()).right()!!.first)
        assertEquals(listOf('A', 'A'), many(charA)("AABC".asSequence()).right()!!.first)
        assertEquals(listOf('A', 'A', 'A', 'A', 'A'), many(charA)("AAAAABC".asSequence()).right()!!.first)
        assertEquals(emptyList<Char>(), many(charA)("BC".asSequence()).right()!!.first)
    }
}

class OptionalCombinatorTest {
    @Test
    fun optional() {
        assertEquals('A', optional(charA)("AB".asSequence()).right()!!.first)
        assertEquals(null, optional(charA)("B".asSequence()).right()!!.first)
        assertEquals('A', optional(optional(charA))("ABC".asSequence()).right()!!.first)
        assertEquals(null, optional(optional(charB))("ABC".asSequence()).right()!!.first)
    }
}

class OrCombinatorTest {
    @Test
    fun or() {
        assertEquals(Either.left('A'), (charA or charB)("AB".asSequence()).right()!!.first)
        assertEquals(Either.right('B'), (charA or charB)("BC".asSequence()).right()!!.first)
        assertEquals(Either.left('A'), (charA or charB)("A".asSequence()).right()!!.first)
        assertEquals(Either.right('A'), (charB or charA)("A".asSequence()).right()!!.first)
        assertEquals(Either.right('C'), (charA or charB or charC)("C".asSequence()).right()!!.first)
        assertEquals("Expecting one of 'A','B' but '' was found.", (charA or charB)("".asSequence()).left()!!.message)
        assertEquals("Expecting one of 'A','B' but 'C' was found.", (charA or charB)("C".asSequence()).left()!!.message)
    }
}
