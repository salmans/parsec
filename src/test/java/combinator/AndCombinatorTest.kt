package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class AndCombinatorTest {
    @Test
    fun parse() {
        assertEquals(('A' to 'B'), and(charA, charB)("AB".asSequence()).right()!!.first)
        assertEquals(('A' to 'B'), and(charA, charB)("ABC".asSequence()).right()!!.first)
        assertEquals((('A' to 'B') to 'C'), and(and(charA, charB), charC)("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", and(charA, charB)("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", and(charA, charB)("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", and(charA, charB)("AC".asSequence()).left()!!.message)
    }
}

class LeftCombinatorTest {
    @Test
    fun parse() {
        assertEquals('A', left(charA, charB)("AB".asSequence()).right()!!.first)
        assertEquals('A', left(charA, charB)("ABC".asSequence()).right()!!.first)
        assertEquals('A', left(left(charA, charB), charC)("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", left(charA, charB)("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", left(charA, charB)("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", left(charA, charB)("AC".asSequence()).left()!!.message)
    }
}

class RightCombinatorTest {
    @Test
    fun parse() {
        assertEquals('B', right(charA, charB)("AB".asSequence()).right()!!.first)
        assertEquals('B', right(charA, charB)("ABC".asSequence()).right()!!.first)
        assertEquals('C', right(right(charA, charB), charC)("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", right(charA, charB)("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", right(charA, charB)("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", right(charA, charB)("AC".asSequence()).left()!!.message)
    }
}