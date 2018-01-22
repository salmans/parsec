package parsec

import org.junit.Assert.*
import org.junit.Test

class AndCombinatorTest {
    @Test
    fun parse() {
        assertEquals(('A' to 'B'), (charA and charB).parse("AB".asSequence()).right()!!.first)
        assertEquals(('A' to 'B'), (charA and charB).parse("ABC".asSequence()).right()!!.first)
        assertEquals((('A' to 'B') to 'C'), (charA and charB and charC).parse("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA and charB).parse("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA and charB).parse("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA and charB).parse("AC".asSequence()).left()!!.message)
    }
}

class LeftCombinatorTest {
    @Test
    fun parse() {
        assertEquals('A', (charA left charB).parse("AB".asSequence()).right()!!.first)
        assertEquals('A', (charA left charB).parse("ABC".asSequence()).right()!!.first)
        assertEquals('A', (charA left charB left charC).parse("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA left charB).parse("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA left charB).parse("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA left charB).parse("AC".asSequence()).left()!!.message)
    }
}

class RightCombinatorTest {
    @Test
    fun parse() {
        assertEquals('B', (charA right charB).parse("AB".asSequence()).right()!!.first)
        assertEquals('B', (charA right charB).parse("ABC".asSequence()).right()!!.first)
        assertEquals('C', (charA right charB right charC).parse("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA right charB).parse("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA right charB).parse("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA right charB).parse("AC".asSequence()).left()!!.message)
    }
}