package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class ManyCombinatorTest {
    @Test
    fun parse() {
        assertEquals(listOf('A'), many(charA)("AB".asSequence()).right()!!.first)
        assertEquals(listOf('A', 'A'), many(charA)("AABC".asSequence()).right()!!.first)
        assertEquals(listOf('A', 'A', 'A', 'A', 'A'), many(charA)("AAAAABC".asSequence()).right()!!.first)
        assertEquals(emptyList<Char>(), many(charA)("BC".asSequence()).right()!!.first)
    }
}