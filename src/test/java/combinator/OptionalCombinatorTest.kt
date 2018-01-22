package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class OptionalCombinatorTest {
    @Test
    fun parse() {
        assertEquals('A', optional { charA }.parse("AB".asSequence()).right()!!.first)
        assertEquals(null, optional { charA }.parse("B".asSequence()).right()!!.first)
        assertEquals('A', optional { optional { charA } }.parse("ABC".asSequence()).right()!!.first)
        assertEquals(null, optional { optional { charB } }.parse("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'B' but '' was found.", (charA and charB).parse("A".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", (charA and charB).parse("BC".asSequence()).left()!!.message)
        assertEquals("Expecting 'B' but 'C' was found.", (charA and charB).parse("AC".asSequence()).left()!!.message)
    }
}