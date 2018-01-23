package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class OptionalCombinatorTest {
    @Test
    fun optional() {
        assertEquals('A', optional(charA)("AB".asSequence()).right()!!.first)
        assertEquals(null, optional(charA)("B".asSequence()).right()!!.first)
        assertEquals('A', optional(optional(charA))("ABC".asSequence()).right()!!.first)
        assertEquals(null, optional(optional(charB))("ABC".asSequence()).right()!!.first)
    }
}