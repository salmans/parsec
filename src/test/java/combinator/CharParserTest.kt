package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class CharParserTest {
    @Test
    fun char() {
        assertEquals('A', charA("A".asSequence()).first.right()!!)
        assertEquals('A', charA("ABC".asSequence()).first.right()!!)
        assertEquals("Expecting 'A' but 'end of input' was found.", failure("".asSequence(), charA))
        assertEquals("Expecting 'A' but 'B' was found.", failure("BA".asSequence(), charA))
    }
}