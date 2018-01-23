package combinator

import org.junit.Assert.assertEquals
import org.junit.Test
import tools.Either

class OrCombinatorTest {
    @Test
    fun parse() {
        assertEquals(Either.left('A'), or(charA, charB)("AB".asSequence()).right()!!.first)
        assertEquals(Either.right('B'), or(charA, charB)("BC".asSequence()).right()!!.first)
        assertEquals(Either.left('A'), or(charA, charB)("A".asSequence()).right()!!.first)
        assertEquals(Either.right('A'), or(charB, charA)("A".asSequence()).right()!!.first)
        assertEquals(Either.right('C'), or(or(charA, charB), charC)("C".asSequence()).right()!!.first)
        assertEquals("Expecting one of 'A','B' but '' was found.", or(charA, charB)("".asSequence()).left()!!.message)
        assertEquals("Expecting one of 'A','B' but 'C' was found.", or(charA, charB)("C".asSequence()).left()!!.message)
    }
}
