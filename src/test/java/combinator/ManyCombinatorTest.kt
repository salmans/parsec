package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class ManyCombinatorTest {
    @Test
    fun parse() {
        assertEquals(listOf('A'), (many { charA }).parse("AB".asSequence()).right()!!.first)
        assertEquals(listOf('A', 'A'), (many { charA }).parse("AABC".asSequence()).right()!!.first)
        assertEquals(listOf('A', 'A', 'A', 'A', 'A'), (many { charA }).parse("AAAAABC".asSequence()).right()!!.first)
        assertEquals(emptyList<Char>(), (many { charA }).parse("BC".asSequence()).right()!!.first)
    }
}