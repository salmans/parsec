package combinator

import org.junit.Assert.*
import org.junit.Test

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